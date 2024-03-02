package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.entity.merchant.RebateConfig;
import com.xiliulou.electricity.entity.merchant.RebateRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.model.MerchantModify;
import com.xiliulou.electricity.service.merchant.MerchantLevelService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.RebateConfigService;
import com.xiliulou.electricity.service.merchant.RebateRecordService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;

/**
 * 商户升级后重新计算返利差额 计算返利差额，仅自动升级需要计算差额，后台手动修改不需要重新计算差额 原型需求：实时触发升级，升级到新等级后，当月的所有骑手均按新的返利规则返利；已结算的返利记录不变，需按新等级与升级前等级返利计算差额部分，添加“未结算”状态、“差额”类型的返利记录中；
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-20-11:08
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.MERCHANT_MODIFY_TOPIC, consumerGroup = MqConsumerConstant.MERCHANT_MODIFY_CONSUMER_GROUP, consumeThreadMax = 3)
public class MerchantModifyConsumer implements RocketMQListener<String> {
    
    @Autowired
    private RebateConfigService rebateConfigService;
    
    @Autowired
    private MerchantService merchantService;
    
    @Autowired
    private RebateRecordService rebateRecordService;
    
    @Autowired
    private MerchantLevelService merchantLevelService;
    
    @Override
    public void onMessage(String message) {
        log.info("MERCHANT MODIFY CONSUMER INFO!received msg={}", message);
        MerchantModify merchantModify = null;
        
        try {
            merchantModify = JsonUtil.fromJson(message, MerchantModify.class);
        } catch (Exception e) {
            log.error("MERCHANT MODIFY CONSUMER ERROR!parse fail,msg={}", message, e);
        }
        
        if (Objects.isNull(merchantModify) || Objects.isNull(merchantModify.getMerchantId())) {
            return;
        }
        
        Merchant merchant = merchantService.queryByIdFromCache(merchantModify.getMerchantId());
        if (Objects.isNull(merchant)) {
            log.warn("MERCHANT MODIFY CONSUMER WARN!merchant is null,merchantId={}", merchantModify.getMerchantId());
            return;
        }
        
        if (Objects.equals(MerchantConstant.DISABLE, merchant.getStatus())) {
            log.warn("MERCHANT MODIFY CONSUMER WARN!merchant is disable,merchantId={}", merchantModify.getMerchantId());
            return;
        }
        
        MerchantLevel merchantLevel = merchantLevelService.queryById(merchant.getMerchantGradeId());
        if (Objects.isNull(merchantLevel)) {
            log.warn("MERCHANT MODIFY CONSUMER WARN!merchantLevel is null,merchantId={}", merchantModify.getMerchantId());
            return;
        }
        
        //当前商户等级
        String currentLevel = merchantLevel.getLevel();
        
        int offset = 0;
        int size = 200;
        
        long startTime = DateUtils.getDayStartTimeByLocalDate(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()));
        long endTime = DateUtils.getDayStartTimeByLocalDate(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())) + 24 * 60 * 60 * 1000L;
        
        while (true) {
            List<RebateRecord> list = rebateRecordService.listCurrentMonthRebateRecord(startTime, endTime, offset, size);
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            
            list.forEach(item -> {
                //获取最新返利规则
                RebateConfig rebateConfig = rebateConfigService.queryByMidAndMerchantLevel(item.getMemberCardId(), currentLevel);
                if (Objects.isNull(rebateConfig)) {
                    log.warn("MERCHANT MODIFY CONSUMER WARN!rebateConfig is null,id={},memberCardId={},level={}", item.getId(), item.getMemberCardId(), currentLevel);
                    return;
                }
    
                if(Objects.equals( rebateConfig.getStatus(), MerchantConstant.REBATE_DISABLE)){
                    log.warn("MERCHANT MODIFY CONSUMER WARN!rebateConfig is disable,id={},memberCardId={},level={}", item.getId(), item.getMemberCardId(), currentLevel);
                    return;
                }
    
                if (Integer.parseInt(item.getLevel()) <= Integer.parseInt(currentLevel)) {
                    return;
                }
                
                BigDecimal newMerchantRebate =
                        Objects.equals(item.getType(), MerchantConstant.MERCHANT_REBATE_TYPE_INVITATION) ? rebateConfig.getMerchantInvitation() : rebateConfig.getMerchantRenewal();
                BigDecimal newChannelerRebate = Objects.equals(item.getType(), MerchantConstant.MERCHANT_REBATE_TYPE_INVITATION) ? rebateConfig.getChannelerInvitation()
                        : rebateConfig.getChannelerRenewal();
                
                BigDecimal oldMerchantRebate = item.getMerchantRebate();
                BigDecimal oldChannelerRebate = item.getChannelerRebate();
                
                RebateRecord rebateRecord = new RebateRecord();
                rebateRecord.setUid(item.getUid());
                rebateRecord.setName(item.getName());
                rebateRecord.setPhone(item.getPhone());
                rebateRecord.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_REBATE, rebateRecord.getUid()));
                rebateRecord.setOriginalOrderId(item.getOrderId());
                rebateRecord.setMemberCardId(item.getMemberCardId());
                rebateRecord.setMemberCardName(item.getMemberCardName());
                rebateRecord.setType(MerchantConstant.MERCHANT_REBATE_TYPE_DISCREPANCY);
                rebateRecord.setFranchiseeId(item.getFranchiseeId());
                rebateRecord.setLevel(currentLevel);
                rebateRecord.setMerchantId(item.getMerchantId());
                rebateRecord.setMerchantUid(item.getMerchantUid());
                rebateRecord.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE);
                rebateRecord.setChanneler(item.getChanneler());
                rebateRecord.setChannelerRebate(newChannelerRebate.subtract(oldChannelerRebate));
                rebateRecord.setMerchantRebate(newMerchantRebate.subtract(oldMerchantRebate));
                rebateRecord.setPlaceId(item.getPlaceId());
                rebateRecord.setPlaceUid(item.getPlaceUid());
                rebateRecord.setRebateTime(System.currentTimeMillis());
                rebateRecord.setTenantId(item.getTenantId());
                rebateRecord.setCreateTime(System.currentTimeMillis());
                rebateRecord.setUpdateTime(System.currentTimeMillis());
                rebateRecordService.insert(rebateRecord);
            });
            
            offset += size;
        }
    }
}
