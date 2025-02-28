package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.dto.InstallmentMqCommonDTO;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.event.publish.LostUserActivityDealPublish;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.FyConfigService;
import com.xiliulou.electricity.service.installment.InstallmentBizService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.pay.deposit.fengyun.config.FengYunConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_AGREEMENT_PAY_NOTIFY_LOCK;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_COMPLETED;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.PACKAGE_TYPE_BATTERY;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/12/4 20:04
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.INSTALLMENT_BUSINESS_TOPIC, selectorExpression = MqProducerConstant.INSTALLMENT_DEDUCT_NOTIFY_TAG, consumerGroup = MqConsumerConstant.INSTALLMENT_DEDUCT_NOTIFY_GROUP, consumeThreadMax = 3)
public class InstallmentDeductNotifyConsumer implements RocketMQListener<String> {
    
    @Resource
    private FyConfigService fyConfigService;
    
    @Resource
    private InstallmentBizService installmentBizService;
    
    @Resource
    private InstallmentRecordService installmentRecordService;
    
    @Resource
    private InstallmentDeductionPlanService installmentDeductionPlanService;
    
    @Resource
    private InstallmentTerminatingRecordService installmentTerminatingRecordService;
    
    @Resource
    private RocketMqService rocketMqService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private FengYunConfig fengYunConfig;

    @Resource
    private LostUserActivityDealPublish lostUserActivityDealPublish;
    
    
    @Override
    public void onMessage(String message) {
        if (StringUtils.isBlank(message) || StringUtils.isEmpty(message)) {
            log.error("INSTALLMENT RENEW CONSUMER. message is null or empty");
            return;
        }
        InstallmentMqCommonDTO commonDTO = JsonUtil.fromJson(message, InstallmentMqCommonDTO.class);
        
        String traceId = StringUtils.isEmpty(commonDTO.getTraceId()) ? IdUtil.simpleUUID() : commonDTO.getTraceId();
        MDC.put(CommonConstant.TRACE_ID, traceId);
        
        String externalAgreementNo = commonDTO.getExternalAgreementNo();
        InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNoWithoutUnpaid(externalAgreementNo);
        if (Objects.isNull(installmentRecord)) {
            log.warn("INSTALLMENT RENEW CONSUMER. installmentRecord is null, externalAgreementNo={}", externalAgreementNo);
            return;
        }
        
        Long uid = installmentRecord.getUid();
        
        try {
            Integer issue = commonDTO.getIssue();
            
            if (Objects.isNull(issue)) {
                log.warn("INSTALLMENT RENEW CONSUMER. param is null, externalAgreementNo={}", externalAgreementNo);
                return;
            }
            
            List<InstallmentDeductionPlan> deductionPlanList = installmentDeductionPlanService.listByExternalAgreementNoAndIssue(installmentRecord.getTenantId(),
                    externalAgreementNo, issue);
            if (CollectionUtils.isEmpty(deductionPlanList)) {
                log.warn("INSTALLMENT RENEW CONSUMER. deductionPlanList is null, externalAgreementNo={}", externalAgreementNo);
                return;
            }
            
            // 判断是否可以开始续费套餐，若代扣计划没有全部代扣成功，则重新发送5s延迟消息，重试
            for (InstallmentDeductionPlan deductionPlan : deductionPlanList) {
                if (!Objects.equals(deductionPlan.getStatus(), InstallmentConstants.DEDUCTION_PLAN_STATUS_PAID)) {
                    retry(commonDTO);
                    return;
                }
            }
            
            // 已成功续费套餐，不需再执行后续逻辑
            Integer paidInstallment = installmentRecord.getPaidInstallment();
            if (Objects.isNull(paidInstallment)) {
                log.error("INSTALLMENT RENEW CONSUMER. paidInstallment is null, Something has gone wrong and needs to be resolved immediately. externalAgreementNo={}", externalAgreementNo);
                return;
            }
            
            // 套餐续费成功之后才会修改已支付期数，出现如下场景代表本期已续费成功
            if (paidInstallment >= issue) {
                return;
            }
            
            // 加锁避免多续费，因为获取锁失败的处理不可重试，若重试会导致多续费套餐，只要有一个消息获取到锁走了后续处理套餐就能续费成功，若续费失败代表数据异常重试也是继续失败
            // 锁不可释放，重试间隔为5s，去间隔时间三倍加锁不释放可以极大程度避免同一期续费两次套餐
            if (!redisService.setNx(String.format(CACHE_INSTALLMENT_AGREEMENT_PAY_NOTIFY_LOCK, uid, issue), "1", 15 * 1000L, false)) {
                return;
            }
            
            Triple<Boolean, String, Object> handlePackageTriple = null;
            if (Objects.equals(installmentRecord.getPackageType(), PACKAGE_TYPE_BATTERY)) {
                // 处理换电代扣成功的场景
                handlePackageTriple = installmentBizService.handleBatteryMemberCard(installmentRecord, deductionPlanList, uid, null);
            }
            
            // 代扣成功后其他记录的处理
            if (Objects.nonNull(handlePackageTriple) && handlePackageTriple.getLeft()) {

                InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
                installmentRecordUpdate.setId(installmentRecord.getId());
                // 若全部代扣完，改为已完成，并且解约
                if (Objects.equals(installmentRecord.getInstallmentNo(), issue)) {
                    installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_COMPLETED);
                }
                installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
                installmentRecordUpdate.setPaidInstallment(paidInstallment + 1);
                installmentRecordService.update(installmentRecordUpdate);

                if (!Objects.equals(deductionPlanList.get(0).getIssue(), 1)) {
                    String orderId = Objects.nonNull(handlePackageTriple.getRight()) ? (String) handlePackageTriple.getRight() : "";
                    // 流失用户活动处理
                    lostUserActivityDealPublish.publish(uid, YesNoEnum.YES.getCode(), installmentRecord.getTenantId(), orderId);
                }

                if (Objects.equals(installmentRecord.getInstallmentNo(), issue)) {
                    FyConfig config = fyConfigService.queryByTenantIdFromCache(installmentRecord.getTenantId());
                    if (Objects.isNull(config)) {
                        log.error("INSTALLMENT RENEW CONSUMER. no fyConfig, tenantId={}", installmentRecord.getTenantId());
                    }
                    
                    InstallmentTerminatingRecord installmentTerminatingRecord = installmentTerminatingRecordService.generateTerminatingRecord(installmentRecord, "分期套餐代扣完毕",
                            true);
                    installmentTerminatingRecordService.insert(installmentTerminatingRecord);
                    installmentBizService.terminatingInstallmentRecord(installmentRecord, config);
                }
            }
            
        } finally {
            MDC.clear();
        }
    }
    
    private void retry(InstallmentMqCommonDTO commonDTO) {
        Integer retryCount = commonDTO.getRetryCount();
        if (Objects.nonNull(retryCount) && retryCount >= fengYunConfig.getRetryCount()) {
            return;
        }
        
        commonDTO.setRetryCount(Objects.isNull(retryCount) ? 1 : retryCount + 1);
        
        // 延迟5s重试
        rocketMqService.sendAsyncMsg(MqProducerConstant.INSTALLMENT_BUSINESS_TOPIC, JsonUtil.toJson(commonDTO), MqProducerConstant.INSTALLMENT_DEDUCT_NOTIFY_TAG, null, 2);
    }
}
