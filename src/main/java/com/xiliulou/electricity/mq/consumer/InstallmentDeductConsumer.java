package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.dto.InstallmentMqCommonDTO;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.FyConfigService;
import com.xiliulou.electricity.service.installment.InstallmentBizService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.utils.InstallmentUtil;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/12/3 21:07
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.INSTALLMENT_BUSINESS_TOPIC, selectorExpression = MqProducerConstant.INSTALLMENT_DEDUCT_TAG, consumerGroup = MqConsumerConstant.INSTALLMENT_DEDUCT_GROUP, consumeThreadMax = 3)
public class InstallmentDeductConsumer implements RocketMQListener<String> {
    
    @Resource
    private InstallmentRecordService installmentRecordService;
    
    @Resource
    private InstallmentDeductionPlanService installmentDeductionPlanService;
    
    @Resource
    private InstallmentBizService installmentBizService;
    
    @Resource
    private FyConfigService fyConfigService;
    
    @Resource
    private RocketMqService rocketMqService;
    
    
    @Override
    public void onMessage(String message) {
        if (StringUtils.isBlank(message) || StringUtils.isEmpty(message)) {
            log.error("INSTALLMENT DEDUCT CONSUMER. message is null or empty");
            return;
        }
        InstallmentMqCommonDTO commonDTO = JsonUtil.fromJson(message, InstallmentMqCommonDTO.class);
        
        String traceId = StringUtils.isEmpty(commonDTO.getTraceId()) ? IdUtil.simpleUUID() : commonDTO.getTraceId();
        MDC.put(CommonConstant.TRACE_ID, traceId);
        
        try {
            InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(commonDTO.getExternalAgreementNo());
            if (Objects.isNull(installmentRecord)) {
                log.warn("INSTALLMENT DEDUCT CONSUMER. installmentRecord is null, externalAgreementNo={}", commonDTO.getExternalAgreementNo());
                retry(commonDTO);
                return;
            }
            
            FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(installmentRecord.getTenantId());
            if (Objects.isNull(fyConfig) || StrUtil.isBlank(fyConfig.getMerchantCode()) || StrUtil.isEmpty(fyConfig.getStoreCode()) || StrUtil.isEmpty(fyConfig.getChannelCode())) {
                log.error("INSTALLMENT DEDUCT CONSUMER. FyConfig is wrong, externalAgreementNo={}", commonDTO.getExternalAgreementNo());
                return;
            }
            
            List<InstallmentDeductionPlan> deductionPlanList = installmentDeductionPlanService.listByExternalAgreementNoAndIssue(installmentRecord.getTenantId(),
                    installmentRecord.getExternalAgreementNo(), 1);
            if (CollectionUtils.isEmpty(deductionPlanList)) {
                log.warn("INSTALLMENT DEDUCT CONSUMER. deductionPlanList is null, externalAgreementNo={}", commonDTO.getExternalAgreementNo());
                retry(commonDTO);
                return;
            }
            
            // 生成payNo
            InstallmentUtil.generatePayNo(installmentRecord.getUid(), deductionPlanList);
            
            installmentBizService.initiatingDeduct(deductionPlanList, installmentRecord, fyConfig);
        } finally {
            MDC.clear();
        }
    }
    
    private void retry(InstallmentMqCommonDTO commonDTO) {
        if (Objects.nonNull(commonDTO) && commonDTO.getRetryCount() > 0) {
            return;
        }
        
        commonDTO.setRetryCount(1);
        // 延迟5s重试
        rocketMqService.sendAsyncMsg(MqProducerConstant.INSTALLMENT_BUSINESS_TOPIC, JsonUtil.toJson(commonDTO), MqProducerConstant.INSTALLMENT_DEDUCT_TAG, null, 2);
    }
}
