package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.entity.merchant.RebateConfig;
import com.xiliulou.electricity.entity.merchant.RebateRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.model.BatteryMemberCardMerchantRebate;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeAmountService;
import com.xiliulou.electricity.service.merchant.MerchantLevelService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.MerchantUserAmountService;
import com.xiliulou.electricity.service.merchant.RebateConfigService;
import com.xiliulou.electricity.service.merchant.RebateRecordService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 购买套餐返利
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-19-13:51
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.BATTERY_MEMBER_CARD_MERCHANT_REBATE_TOPIC, consumerGroup = MqConsumerConstant.BATTERY_MEMBER_CARD_MERCHANT_REBATE_GROUP, consumeThreadMax = 5)
public class BatteryMemberCardMerchantRebateConsumer implements RocketMQListener<String> {
    
    @Autowired
    private UserInfoExtraService userInfoExtraService;
    
    @Autowired
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    private BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Autowired
    private MerchantService merchantService;
    
    @Autowired
    private RebateConfigService rebateConfigService;
    
    @Autowired
    private MerchantLevelService merchantLevelService;
    
    @Autowired
    private RebateRecordService rebateRecordService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EleRefundOrderService eleRefundOrderService;
    
    @Autowired
    private ChannelEmployeeAmountService channelEmployeeAmountService;
    
    @Autowired
    private MerchantUserAmountService merchantUserAmountService;
    
    @Override
    public void onMessage(String message) {
        
        log.info("REBATE CONSUMER INFO!received msg={}", message);
        BatteryMemberCardMerchantRebate batteryMemberCardMerchantRebate = null;
        
        try {
            batteryMemberCardMerchantRebate = JsonUtil.fromJson(message, BatteryMemberCardMerchantRebate.class);
        } catch (Exception e) {
            log.error("REBATE CONSUMER ERROR!parse fail,msg={}", message, e);
        }
        
        if (Objects.isNull(batteryMemberCardMerchantRebate) || Objects.isNull(batteryMemberCardMerchantRebate.getType())) {
            return;
        }
        
        if (Objects.equals(MerchantConstant.TYPE_PURCHASE, batteryMemberCardMerchantRebate.getType())) {
            //返利
            handleRebate(batteryMemberCardMerchantRebate);
        } else {
            //退租
            handleMemberCardRentRefund(batteryMemberCardMerchantRebate);
        }
    }
    
    private void handleRebate(BatteryMemberCardMerchantRebate batteryMemberCardMerchantRebate) {
        
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(batteryMemberCardMerchantRebate.getOrderId());
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("REBATE CONSUMER WARN!not found electricityMemberCardOrder,orderId={}", batteryMemberCardMerchantRebate.getOrderId());
            return;
        }
        
        //若用户退过押金，再次缴纳押金后，不返利
        if (Objects.nonNull(eleRefundOrderService.existsRefundOrderByUid(electricityMemberCardOrder.getUid()))) {
            log.warn("REBATE CONSUMER WARN!user exists refund order,uid={}", batteryMemberCardMerchantRebate.getUid());
            return;
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("REBATE CONSUMER WARN!not found userInfo,uid={}", batteryMemberCardMerchantRebate.getUid());
            return;
        }
        
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfoExtra)) {
            log.warn("REBATE CONSUMER WARN!not found userInfoExtra,uid={}", electricityMemberCardOrder.getUid());
            return;
        }
        
        if (Objects.isNull(userInfoExtra.getMerchantId())) {
            log.warn("REBATE CONSUMER WARN!not found merchantId,uid={}", electricityMemberCardOrder.getUid());
            return;
        }
        
        Merchant merchant = merchantService.queryByIdFromCache(userInfoExtra.getMerchantId());
        if (Objects.isNull(merchant)) {
            log.warn("REBATE CONSUMER WARN!not found merchant,uid={}", electricityMemberCardOrder.getUid());
            return;
        }
        
        //渠道员
        User channel = userService.queryByUidFromCache(merchant.getChannelEmployeeUid());
        
        //若商户及渠道员均禁用，不返利
        if (Objects.equals(MerchantConstant.DISABLE, merchant.getStatus()) && Objects.nonNull(channel) && Objects.equals(channel.getLockFlag(), User.USER_LOCK)) {
            log.warn("REBATE CONSUMER WARN!merchant disable,channel disable,uid={},merchantId={}", electricityMemberCardOrder.getUid(), userInfoExtra.getMerchantId());
            return;
        }
        
        //获取商户等级
        MerchantLevel merchantLevel = merchantLevelService.queryById(merchant.getMerchantGradeId());
        if (Objects.isNull(merchantLevel)) {
            log.warn("REBATE CONSUMER WARN!not found merchantLevel,uid={},merchantId={}", electricityMemberCardOrder.getUid(), userInfoExtra.getMerchantId());
            return;
        }
        
        //获取返利配置
        RebateConfig rebateConfig = rebateConfigService.queryByMidAndMerchantLevel(electricityMemberCardOrder.getMemberCardId(), merchantLevel.getLevel());
        if (Objects.isNull(rebateConfig)) {
            log.warn("REBATE CONSUMER WARN!not found rebateConfig,uid={},mid={}", electricityMemberCardOrder.getUid(), electricityMemberCardOrder.getMemberCardId());
            return;
        }
        
        if (Objects.equals(rebateConfig.getStatus(), MerchantConstant.REBATE_DISABLE)) {
            log.warn("REBATE CONSUMER WARN!rebateConfig is disable,uid={},mid={}", electricityMemberCardOrder.getUid(), electricityMemberCardOrder.getMemberCardId());
            return;
        }
        
        //商户返现金额
        BigDecimal merchantRebate = null;
        
        //渠道员返现金额
        BigDecimal channelerRebate = null;
        
        //返现类型
        Integer type = null;
        
        Integer payCount = electricityMemberCardOrder.getPayCount();
        if (payCount <= 1) {
            type = MerchantConstant.MERCHANT_REBATE_TYPE_INVITATION;
            //拉新返现
            merchantRebate = rebateConfig.getMerchantInvitation();
            channelerRebate = rebateConfig.getChannelerInvitation();
        } else {
            type = MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL;
            //续费返现
            merchantRebate = rebateConfig.getMerchantRenewal();
            channelerRebate = rebateConfig.getChannelerRenewal();
        }
        
        RebateRecord rebateRecord = new RebateRecord();
        rebateRecord.setUid(electricityMemberCardOrder.getUid());
        rebateRecord.setName(electricityMemberCardOrder.getUserName());
        rebateRecord.setPhone(userInfo.getPhone());
        rebateRecord.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_REBATE, userInfo.getUid()));
        rebateRecord.setOriginalOrderId(electricityMemberCardOrder.getOrderId());
        rebateRecord.setMemberCardId(electricityMemberCardOrder.getMemberCardId());
        rebateRecord.setType(type);
        rebateRecord.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
        rebateRecord.setLevel(merchantLevel.getLevel());
        rebateRecord.setMerchantId(merchant.getId());
        rebateRecord.setMerchantUid(merchant.getUid());
        rebateRecord.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE);
        rebateRecord.setChanneler(userInfoExtra.getChannelEmployeeUid());
        rebateRecord.setChannelerRebate(channelerRebate);
        rebateRecord.setMerchantRebate(merchantRebate);
        rebateRecord.setPlaceId(userInfoExtra.getPlaceId());
        rebateRecord.setPlaceUid(userInfoExtra.getPlaceUid());
        rebateRecord.setRebateTime(electricityMemberCardOrder.getCreateTime());
        rebateRecord.setTenantId(electricityMemberCardOrder.getTenantId());
        rebateRecord.setCreateTime(System.currentTimeMillis());
        rebateRecord.setUpdateTime(System.currentTimeMillis());
        
        //商户禁用后，不给商户返利；渠道员禁用，不返利
        if (Objects.equals(MerchantConstant.DISABLE, merchant.getStatus())) {
            rebateRecord.setMerchantRebate(BigDecimal.ZERO);
        }
        
        if (Objects.isNull(channel) || Objects.equals(channel.getLockFlag(), User.USER_LOCK)) {
            rebateRecord.setChannelerRebate(BigDecimal.ZERO);
        }
        
        rebateRecordService.insert(rebateRecord);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void handleMemberCardRentRefund(BatteryMemberCardMerchantRebate batteryMemberCardMerchantRebate) {
        
        BatteryMembercardRefundOrder batteryMembercardRefundOrder = batteryMembercardRefundOrderService.selectByRefundOrderNo(batteryMemberCardMerchantRebate.getOrderId());
        if (Objects.isNull(batteryMembercardRefundOrder)) {
            log.warn("REBATE REFUND CONSUMER WARN!not found batteryMemberCardRefundOrder,orderId={}", batteryMemberCardMerchantRebate.getOrderId());
            return;
        }
        
        //获取返利记录
        RebateRecord rebateRecord = rebateRecordService.queryByOriginalOrderId(batteryMembercardRefundOrder.getMemberCardOrderNo());
        if (Objects.isNull(rebateRecord)) {
            log.warn("REBATE REFUND CONSUMER WARN!not found rebateRecord,memberCardOrderId={}", batteryMembercardRefundOrder.getMemberCardOrderNo());
            return;
        }
        
        RebateRecord rebateRecordInsert = new RebateRecord();
        rebateRecordInsert.setUid(rebateRecord.getUid());
        rebateRecordInsert.setName(rebateRecord.getName());
        rebateRecordInsert.setPhone(rebateRecord.getPhone());
        rebateRecordInsert.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_REBATE, rebateRecord.getUid()));
        rebateRecordInsert.setOriginalOrderId(batteryMembercardRefundOrder.getRefundOrderNo());
        rebateRecordInsert.setMemberCardId(rebateRecord.getMemberCardId());
        rebateRecordInsert.setMemberCardName(rebateRecord.getMemberCardName());
        rebateRecordInsert.setType(rebateRecord.getType());
        rebateRecordInsert.setFranchiseeId(rebateRecord.getFranchiseeId());
        rebateRecordInsert.setLevel(rebateRecord.getLevel());
        rebateRecordInsert.setMerchantId(rebateRecord.getMerchantId());
        rebateRecordInsert.setMerchantUid(rebateRecord.getMerchantUid());
        rebateRecordInsert.setChanneler(rebateRecord.getChanneler());
        rebateRecordInsert.setChannelerRebate(rebateRecord.getChannelerRebate());
        rebateRecordInsert.setMerchantRebate(rebateRecord.getMerchantRebate());
        rebateRecordInsert.setPlaceId(rebateRecord.getPlaceId());
        rebateRecordInsert.setPlaceUid(rebateRecord.getPlaceUid());
        rebateRecordInsert.setRebateTime(batteryMembercardRefundOrder.getCreateTime());
        rebateRecordInsert.setTenantId(rebateRecord.getTenantId());
        rebateRecordInsert.setCreateTime(System.currentTimeMillis());
        rebateRecordInsert.setUpdateTime(System.currentTimeMillis());
        
        //未结算，生成 已失效 返利记录
        if (Objects.equals(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE, rebateRecord.getStatus())) {
            rebateRecordInsert.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_EXPIRED);
        }
        
        //返利记录已结算，重新生成 已退回 返利记录，同时扣减返利金额
        if (Objects.equals(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED, rebateRecord.getStatus())) {
            rebateRecordInsert.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED);
            
            //扣减商户、渠道商返利金额
            merchantUserAmountService.reduceAmount(rebateRecord.getMerchantRebate(), rebateRecord.getMerchantUid(), rebateRecord.getTenantId().longValue());
            channelEmployeeAmountService.reduceAmount(rebateRecord.getChannelerRebate(), rebateRecord.getChanneler(), rebateRecord.getTenantId().longValue());
        }
        
        rebateRecordService.insert(rebateRecord);
        
        handleExcessRebateRecord(rebateRecord);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void handleExcessRebateRecord(RebateRecord rebateRecord) {
        //获取差额记录
        List<RebateRecord> excessList = new ArrayList<>();
        String originalOrderId = rebateRecord.getOrderId();
        while (true) {
            RebateRecord excessRecord = rebateRecordService.queryByOriginalOrderId(originalOrderId);
            if (Objects.isNull(excessRecord)) {
                break;
            }
            originalOrderId = excessRecord.getOrderId();
            excessList.add(excessRecord);
        }
        
        if (CollectionUtils.isEmpty(excessList)) {
            return;
        }
        
        for (RebateRecord record : excessList) {
            RebateRecord rebateRecordInsert = new RebateRecord();
            rebateRecordInsert.setUid(record.getUid());
            rebateRecordInsert.setName(record.getName());
            rebateRecordInsert.setPhone(record.getPhone());
            rebateRecordInsert.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_REBATE, rebateRecord.getUid()));
            rebateRecordInsert.setOriginalOrderId(record.getOrderId());
            rebateRecordInsert.setMemberCardId(record.getMemberCardId());
            rebateRecordInsert.setMemberCardName(record.getMemberCardName());
            rebateRecordInsert.setType(record.getType());
            rebateRecordInsert.setFranchiseeId(record.getFranchiseeId());
            rebateRecordInsert.setLevel(record.getLevel());
            rebateRecordInsert.setMerchantId(record.getMerchantId());
            rebateRecordInsert.setMerchantUid(record.getMerchantUid());
            rebateRecordInsert.setChanneler(record.getChanneler());
            rebateRecordInsert.setChannelerRebate(record.getChannelerRebate());
            rebateRecordInsert.setMerchantRebate(record.getMerchantRebate());
            rebateRecordInsert.setPlaceId(record.getPlaceId());
            rebateRecordInsert.setPlaceUid(record.getPlaceUid());
            rebateRecordInsert.setRebateTime(record.getCreateTime());
            rebateRecordInsert.setTenantId(record.getTenantId());
            rebateRecordInsert.setCreateTime(System.currentTimeMillis());
            rebateRecordInsert.setUpdateTime(System.currentTimeMillis());
            rebateRecordInsert.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED);
            
            //扣减商户、渠道商返利金额
            merchantUserAmountService.reduceAmount(rebateRecord.getMerchantRebate(), rebateRecord.getMerchantUid(), rebateRecord.getTenantId().longValue());
            channelEmployeeAmountService.reduceAmount(rebateRecord.getChannelerRebate(), rebateRecord.getChanneler(), rebateRecord.getTenantId().longValue());
            
            rebateRecordService.insert(rebateRecord);
        }
    }
}
