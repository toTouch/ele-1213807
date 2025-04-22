package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.dto.InstallmentMqCommonDTO;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_DEDUCT_LOCK;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_DEDUCTING;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_FAIL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_RECORD_STATUS_FAIL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_RECORD_STATUS_INIT;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/12/5 21:30
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.INSTALLMENT_BUSINESS_TOPIC, selectorExpression = MqProducerConstant.INSTALLMENT_DEDUCT_FAIL_TAG, consumerGroup = MqConsumerConstant.INSTALLMENT_DEDUCT_FAIL_GROUP, consumeThreadMax = 3)
public class InstallmentDeductFailConsumer implements RocketMQListener<String> {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private InstallmentDeductionPlanService installmentDeductionPlanService;
    
    @Resource
    private InstallmentDeductionRecordService installmentDeductionRecordService;
    
    
    @Override
    public void onMessage(String message) {
        if (StringUtils.isBlank(message) || StringUtils.isEmpty(message)) {
            log.error("INSTALLMENT DEDUCT FAIL CONSUMER. message is null or empty");
            return;
        }
        InstallmentMqCommonDTO commonDTO = JsonUtil.fromJson(message, InstallmentMqCommonDTO.class);
        
        String traceId = StringUtils.isEmpty(commonDTO.getTraceId()) ? IdUtil.simpleUUID() : commonDTO.getTraceId();
        MDC.put(CommonConstant.TRACE_ID, traceId);
        
        try {
            Long deductionPlanId = commonDTO.getDeductionPlanId();
            Long deductionRecordId = commonDTO.getDeductionRecordId();
            
            InstallmentDeductionPlan deductionPlan = installmentDeductionPlanService.queryById(deductionPlanId);
            handleDeductPlanFail(deductionPlan);
            
            InstallmentDeductionRecord deductionRecord = installmentDeductionRecordService.queryById(deductionRecordId);
            if (Objects.isNull(deductionRecord)) {
                return;
            }
            // 不能取反return掉，需要释放锁
            if (Objects.equals(deductionRecord.getStatus(), DEDUCTION_RECORD_STATUS_INIT)) {
                handleDeductRecordFail(deductionRecord);
            }
            // 释放带扣锁，即使有一次代扣多个代扣计划的套餐，这里也可以直接释放了，不用等待全部都修改成失败再释放
            redisService.delete(String.format(CACHE_INSTALLMENT_DEDUCT_LOCK, deductionRecord.getUid()));
        } catch (Exception e) {
           log.error("INSTALLMENT DEDUCT FAIL CONSUMER. commonDTO={}", commonDTO, e);
        } finally {
            MDC.clear();
        }
    }
    
    private void handleDeductPlanFail(InstallmentDeductionPlan deductionPlan) {
        if (Objects.isNull(deductionPlan)) {
            return;
        }
        
        // 未支付、代扣中状态的才可以更新为失败状态
        if (!Objects.equals(deductionPlan.getStatus(), DEDUCTION_PLAN_STATUS_INIT) && !Objects.equals(deductionPlan.getStatus(), DEDUCTION_PLAN_STATUS_DEDUCTING)) {
            return;
        }
        
        InstallmentDeductionPlan deductionPlanUpdate = new InstallmentDeductionPlan();
        deductionPlanUpdate.setId(deductionPlan.getId());
        deductionPlanUpdate.setUpdateTime(System.currentTimeMillis());
        deductionPlanUpdate.setStatus(DEDUCTION_PLAN_STATUS_FAIL);
        installmentDeductionPlanService.update(deductionPlanUpdate);
    }
    
    private void handleDeductRecordFail(InstallmentDeductionRecord deductionRecord) {
        InstallmentDeductionRecord deductionRecordUpdate = new InstallmentDeductionRecord();
        deductionRecordUpdate.setId(deductionRecord.getId());
        deductionRecordUpdate.setStatus(DEDUCTION_RECORD_STATUS_FAIL);
        deductionRecordUpdate.setUpdateTime(System.currentTimeMillis());
        installmentDeductionRecordService.update(deductionRecordUpdate);
    }
}
