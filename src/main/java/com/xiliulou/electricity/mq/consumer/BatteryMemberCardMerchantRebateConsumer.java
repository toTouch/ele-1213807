package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantJoinRecordConstant;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantAttr;
import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.entity.merchant.RebateConfig;
import com.xiliulou.electricity.entity.merchant.RebateRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.model.BatteryMemberCardMerchantRebate;
import com.xiliulou.electricity.mq.model.MerchantUpgrade;
import com.xiliulou.electricity.query.merchant.MerchantJoinRecordQueryModel;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeAmountService;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantLevelService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.MerchantUserAmountService;
import com.xiliulou.electricity.service.merchant.RebateConfigService;
import com.xiliulou.electricity.service.merchant.RebateRecordService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
    private RocketMqService rocketMqService;
    
    @Autowired
    private EleRefundOrderService eleRefundOrderService;
    
    @Autowired
    private ChannelEmployeeAmountService channelEmployeeAmountService;
    
    @Autowired
    private MerchantUserAmountService merchantUserAmountService;
    
    @Autowired
    private MerchantJoinRecordService merchantJoinRecordService;
    
    @Autowired
    private MerchantAttrService merchantAttrService;
    @Override
    public void onMessage(String message) {
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());
        
        log.info("REBATE CONSUMER INFO!received msg={}", message);
        BatteryMemberCardMerchantRebate batteryMemberCardMerchantRebate = null;
        
        try {
            batteryMemberCardMerchantRebate = JsonUtil.fromJson(message, BatteryMemberCardMerchantRebate.class);
            
            if (Objects.isNull(batteryMemberCardMerchantRebate) || Objects.isNull(batteryMemberCardMerchantRebate.getType())) {
                return;
            }
            
            if (Objects.equals(MerchantConstant.TYPE_PURCHASE, batteryMemberCardMerchantRebate.getType())) {
                //返利
                handleRebate(batteryMemberCardMerchantRebate);
                
                //续费成功  发送商户升级MQ
                MerchantUpgrade merchantUpgrade = new MerchantUpgrade();
                merchantUpgrade.setUid(batteryMemberCardMerchantRebate.getUid());
                merchantUpgrade.setOrderId(batteryMemberCardMerchantRebate.getOrderId());
                merchantUpgrade.setMerchantId(batteryMemberCardMerchantRebate.getMerchantId());
                rocketMqService.sendAsyncMsg(MqProducerConstant.MERCHANT_UPGRADE_TOPIC, JsonUtil.toJson(merchantUpgrade));
            } else {
                //退租
                handleMemberCardRentRefund(batteryMemberCardMerchantRebate);
            }
        } catch (Exception e) {
            log.error("REBATE CONSUMER ERROR!msg={}", message, e);
        }
    }
    
    private void handleRebate(BatteryMemberCardMerchantRebate batteryMemberCardMerchantRebate) {
        
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(batteryMemberCardMerchantRebate.getOrderId());
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("REBATE CONSUMER WARN!not found electricityMemberCardOrder,orderId={}", batteryMemberCardMerchantRebate.getOrderId());
            return;
        }
        
        //若用户退过押金，再次缴纳押金后，不返利
        if (Objects.nonNull(eleRefundOrderService.existsRefundOrderByUid(batteryMemberCardMerchantRebate.getUid()))) {
            log.warn("REBATE CONSUMER WARN!user exists refund order,uid={}", batteryMemberCardMerchantRebate.getUid());
            return;
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(batteryMemberCardMerchantRebate.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("REBATE CONSUMER WARN!not found userInfo,uid={}", batteryMemberCardMerchantRebate.getUid());
            return;
        }
        
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(batteryMemberCardMerchantRebate.getUid());
        if (Objects.isNull(userInfoExtra)) {
            log.warn("REBATE CONSUMER WARN!not found userInfoExtra,uid={}", batteryMemberCardMerchantRebate.getUid());
            return;
        }
        
        if (Objects.isNull(userInfoExtra.getMerchantId())) {
            log.warn("REBATE CONSUMER WARN!not found merchantId,uid={}", batteryMemberCardMerchantRebate.getUid());
            return;
        }
        
        Merchant merchant = merchantService.queryByIdFromCache(userInfoExtra.getMerchantId());
        if (Objects.isNull(merchant)) {
            log.warn("REBATE CONSUMER WARN!not found merchant,uid={}", batteryMemberCardMerchantRebate.getUid());
            return;
        }
        
        MerchantAttr merchantAttr = merchantAttrService.queryByFranchiseeIdFromCache(electricityMemberCardOrder.getFranchiseeId());
        if (Objects.isNull(merchantAttr)) {
            log.warn("REBATE CONSUMER WARN!not found merchantAttr by tenant,uid={}", batteryMemberCardMerchantRebate.getUid());
            return;
        }
        
        // 默认开启按照新逻辑
        Long channelEmployeeUid = userInfoExtra.getChannelEmployeeUid();
        if (Objects.equals(merchantAttr.getStatus(), MerchantAttr.CLOSE_STATUS)) {
            // 关闭按照之前逻辑，取商户表的渠道员
            channelEmployeeUid = merchant.getChannelEmployeeUid();
        }
        
        //渠道员
        User channel = userService.queryByUidFromCache(channelEmployeeUid);
        
        //若商户及渠道员均禁用，不返利
        if (Objects.equals(MerchantConstant.DISABLE, merchant.getStatus()) && Objects.nonNull(channel) && Objects.equals(channel.getLockFlag(), User.USER_LOCK)) {
            log.warn("REBATE CONSUMER WARN!merchant disable,channel disable,uid={},merchantId={}", batteryMemberCardMerchantRebate.getUid(), userInfoExtra.getMerchantId());
            return;
        }
        
        //获取商户等级
        MerchantLevel merchantLevel = merchantLevelService.queryById(merchant.getMerchantGradeId());
        if (Objects.isNull(merchantLevel)) {
            log.warn("REBATE CONSUMER WARN!not found merchantLevel,uid={},merchantId={}", batteryMemberCardMerchantRebate.getUid(), userInfoExtra.getMerchantId());
            return;
        }
        
        //获取返利配置
        RebateConfig rebateConfig = rebateConfigService.queryByMidAndMerchantLevel(electricityMemberCardOrder.getMemberCardId(), merchantLevel.getLevel());
        if (Objects.isNull(rebateConfig)) {
            log.warn("REBATE CONSUMER WARN!not found rebateConfig,uid={},mid={}", batteryMemberCardMerchantRebate.getUid(), electricityMemberCardOrder.getMemberCardId());
            return;
        }
        
        if (Objects.equals(rebateConfig.getStatus(), MerchantConstant.REBATE_DISABLE)) {
            log.warn("REBATE CONSUMER WARN!rebateConfig is disable,uid={},mid={}", batteryMemberCardMerchantRebate.getUid(), electricityMemberCardOrder.getMemberCardId());
            return;
        }
        
        //商户返现金额
        BigDecimal merchantRebate = null;
        
        //渠道员返现金额
        BigDecimal channelerRebate = null;
        
        //返现类型
        Integer type = null;
        
        //返现类型
        Integer orderType = null;
        
        Integer payCount = electricityMemberCardOrder.getPayCount();
        
        Integer refundType = null;
        
        if (payCount <= 1) {
            type = MerchantConstant.MERCHANT_REBATE_TYPE_INVITATION;
            //拉新返现
            merchantRebate = rebateConfig.getMerchantInvitation();
            channelerRebate = rebateConfig.getChannelerInvitation();
            
            orderType = MerchantConstant.MERCHANT_REBATE_ORDER_TYPE_NEW;
        } else {
            type = MerchantConstant.MERCHANT_REBATE_TYPE_RENEWAL;
            //续费返现
            merchantRebate = rebateConfig.getMerchantRenewal();
            channelerRebate = rebateConfig.getChannelerRenewal();
            
            orderType = MerchantConstant.MERCHANT_REBATE_ORDER_TYPE_OLD;
            refundType = MerchantConstant.REBATE_IS_NOT_REFUND;
        }
        
        RebateRecord rebateRecord = new RebateRecord();
        rebateRecord.setUid(batteryMemberCardMerchantRebate.getUid());
        rebateRecord.setName(electricityMemberCardOrder.getUserName());
        rebateRecord.setPhone(userInfo.getPhone());
        rebateRecord.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_REBATE, userInfo.getUid()));
        rebateRecord.setOriginalOrderId(electricityMemberCardOrder.getOrderId());
        rebateRecord.setMemberCardId(electricityMemberCardOrder.getMemberCardId());
        rebateRecord.setMemberCardName(electricityMemberCardOrder.getCardName());
        rebateRecord.setType(type);
        rebateRecord.setOrderType(orderType);
        rebateRecord.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
        rebateRecord.setLevel(merchantLevel.getLevel());
        rebateRecord.setMerchantId(merchant.getId());
        rebateRecord.setMerchantUid(merchant.getUid());
        rebateRecord.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE);
        rebateRecord.setChanneler(channelEmployeeUid);
        rebateRecord.setChannelerRebate(channelerRebate);
        rebateRecord.setMerchantRebate(merchantRebate);
        rebateRecord.setRefundFlag(refundType);
        rebateRecord.setPlaceId(userInfoExtra.getPlaceId());
        rebateRecord.setPlaceUid(userInfoExtra.getPlaceUid());
        rebateRecord.setRebateTime(System.currentTimeMillis());
        rebateRecord.setTenantId(electricityMemberCardOrder.getTenantId());
        rebateRecord.setCreateTime(System.currentTimeMillis());
        rebateRecord.setUpdateTime(System.currentTimeMillis());
        
        //若渠道员与商户的返利差额都为0  则不生成返利差额记录
        if (BigDecimal.ZERO.compareTo(channelerRebate) == 0 && BigDecimal.ZERO.compareTo(merchantRebate) == 0) {
            log.info("REBATE CONSUMER WARN! Rebate is zero,uid={}", batteryMemberCardMerchantRebate.getUid());
            return;
        }
        
        //商户禁用后，不给商户返利；渠道员禁用，不返利
        if (Objects.equals(MerchantConstant.DISABLE, merchant.getStatus())) {
            rebateRecord.setMerchantRebate(BigDecimal.ZERO);
        }
        
        if (Objects.isNull(channel) || Objects.equals(channel.getLockFlag(), User.USER_LOCK) || Objects.equals(channel.getDelFlag(), User.DEL_DEL)) {
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
        
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(batteryMembercardRefundOrder.getMemberCardOrderNo());
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("REBATE CONSUMER WARN!not found electricityMemberCardOrder,orderId={}", batteryMemberCardMerchantRebate.getOrderId());
            return;
        }
        
        //退租订单数量
        Integer refundOrderNumber = electricityMemberCardOrderService.countRefundOrderByUid(electricityMemberCardOrder.getUid());
        //购买成功订单数量
        Integer successOrderNumber = electricityMemberCardOrderService.countSuccessOrderByUid(electricityMemberCardOrder.getUid());
        
        //若用户购买的套餐全部退租，更新邀请记录为已失效
        if (Objects.equals(refundOrderNumber, successOrderNumber)) {
            MerchantJoinRecordQueryModel merchantJoinRecordQueryModel = new MerchantJoinRecordQueryModel();
            merchantJoinRecordQueryModel.setJoinUid(electricityMemberCardOrder.getUid());
            merchantJoinRecordQueryModel.setStatus(MerchantJoinRecordConstant.STATUS_INVALID);
            merchantJoinRecordQueryModel.setUpdateTime(System.currentTimeMillis());
            merchantJoinRecordService.updateStatus(merchantJoinRecordQueryModel);
        }
        
        //获取返利记录
        RebateRecord rebateRecord = rebateRecordService.queryLatestByOriginalOrderId(batteryMembercardRefundOrder.getMemberCardOrderNo());
        if (Objects.isNull(rebateRecord)) {
            log.warn("REBATE REFUND CONSUMER WARN!not found rebateRecord,memberCardOrderId={}", batteryMembercardRefundOrder.getMemberCardOrderNo());
            return;
        }
        
  /*      //未结算，修改成 已失效 返利记录
        if (Objects.equals(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE, rebateRecord.getStatus())) {
            RebateRecord rebateRecordUpdate = new RebateRecord();
            rebateRecordUpdate.setId(rebateRecord.getId());
            rebateRecordUpdate.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_EXPIRED);
            rebateRecordUpdate.setUpdateTime(System.currentTimeMillis());
            rebateRecordService.updateById(rebateRecordUpdate);
        }*/
    
        // 处理退租的返利记录
        handleRebateRecord(rebateRecord);
        
        //返利记录已结算，重新生成 已退回 返利记录，同时扣减返利金额
        if (Objects.equals(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED, rebateRecord.getStatus())) {
            RebateRecord rebateRecordInsert = new RebateRecord();
            rebateRecordInsert.setUid(rebateRecord.getUid());
            rebateRecordInsert.setName(rebateRecord.getName());
            rebateRecordInsert.setPhone(rebateRecord.getPhone());
            rebateRecordInsert.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_REBATE, rebateRecord.getUid()));
            rebateRecordInsert.setOriginalOrderId(batteryMembercardRefundOrder.getMemberCardOrderNo());
            rebateRecordInsert.setMemberCardId(rebateRecord.getMemberCardId());
            rebateRecordInsert.setMemberCardName(rebateRecord.getMemberCardName());
            rebateRecordInsert.setType(rebateRecord.getType());
            rebateRecordInsert.setOrderType(rebateRecord.getOrderType());
            rebateRecordInsert.setFranchiseeId(rebateRecord.getFranchiseeId());
            rebateRecordInsert.setLevel(rebateRecord.getLevel());
            rebateRecordInsert.setMerchantId(rebateRecord.getMerchantId());
            rebateRecordInsert.setMerchantUid(rebateRecord.getMerchantUid());
            rebateRecordInsert.setChanneler(rebateRecord.getChanneler());
            rebateRecordInsert.setChannelerRebate(rebateRecord.getChannelerRebate());
            rebateRecordInsert.setMerchantRebate(rebateRecord.getMerchantRebate());
            rebateRecordInsert.setPlaceId(rebateRecord.getPlaceId());
            rebateRecordInsert.setPlaceUid(rebateRecord.getPlaceUid());
            rebateRecordInsert.setRebateTime(System.currentTimeMillis());
            rebateRecordInsert.setTenantId(rebateRecord.getTenantId());
            rebateRecordInsert.setCreateTime(System.currentTimeMillis());
            rebateRecordInsert.setUpdateTime(System.currentTimeMillis());
            rebateRecordInsert.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED);
            rebateRecordService.insert(rebateRecordInsert);
            
            //扣减商户、渠道商返利金额
            merchantUserAmountService.reduceAmount(rebateRecord.getMerchantRebate(), rebateRecord.getMerchantUid(), rebateRecord.getTenantId().longValue());
            channelEmployeeAmountService.reduceAmount(rebateRecord.getChannelerRebate(), rebateRecord.getChanneler(), rebateRecord.getTenantId().longValue());
        }
        
        handleExcessRebateRecord(rebateRecord, batteryMembercardRefundOrder);
    }
    
    
    /**
     * 用户退租后需要更新返利记录中的是否退租字段，该字段用于小程序商户端统计续费次数
     * @param rebateRecord
     */
    private void handleRebateRecord(RebateRecord rebateRecord) {
        //未结算，修改成 已失效 返利记录
        RebateRecord rebateRecordUpdate = new RebateRecord();
        rebateRecordUpdate.setId(rebateRecord.getId());
        if (Objects.equals(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE, rebateRecord.getStatus())) {
            rebateRecordUpdate.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_EXPIRED);
        }
        rebateRecordUpdate.setRefundFlag(MerchantConstant.REBATE_IS_REFUND);
        rebateRecordUpdate.setUpdateTime(System.currentTimeMillis());
        rebateRecordService.updateById(rebateRecordUpdate);
    }
    
    /**
     * 处理差额返利记录
     *
     * @param rebateRecord
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleExcessRebateRecord(RebateRecord rebateRecord, BatteryMembercardRefundOrder batteryMembercardRefundOrder) {
        //获取差额记录
        List<RebateRecord> excessList = rebateRecordService.queryByOriginalOrderId(batteryMembercardRefundOrder.getMemberCardOrderNo());
        if (CollectionUtils.isEmpty(excessList)) {
            log.warn("REBATE REFUND CONSUMER WARN!excessList is empty,orderId={}", rebateRecord.getOrderId());
            return;
        }
        
        for (RebateRecord record : excessList) {
            if (!Objects.equals(record.getType(), MerchantConstant.MERCHANT_REBATE_TYPE_DISCREPANCY)) {
                continue;
            }
            
            //未结算
            if (Objects.equals(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE, record.getStatus())) {
                RebateRecord rebateRecordUpdate = new RebateRecord();
                rebateRecordUpdate.setId(record.getId());
                rebateRecordUpdate.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_EXPIRED);
                rebateRecordUpdate.setUpdateTime(System.currentTimeMillis());
                rebateRecordService.updateById(rebateRecordUpdate);
            }
            
            //已结算
            if (Objects.equals(MerchantConstant.MERCHANT_REBATE_STATUS_SETTLED, record.getStatus())) {
                RebateRecord rebateRecordInsert = new RebateRecord();
                rebateRecordInsert.setUid(record.getUid());
                rebateRecordInsert.setName(record.getName());
                rebateRecordInsert.setPhone(record.getPhone());
                rebateRecordInsert.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_REBATE, record.getUid()));
                rebateRecordInsert.setOriginalOrderId(batteryMembercardRefundOrder.getMemberCardOrderNo());
                rebateRecordInsert.setMemberCardId(record.getMemberCardId());
                rebateRecordInsert.setMemberCardName(record.getMemberCardName());
                rebateRecordInsert.setType(record.getType());
                rebateRecordInsert.setOrderType(record.getOrderType());
                rebateRecordInsert.setFranchiseeId(record.getFranchiseeId());
                rebateRecordInsert.setLevel(record.getLevel());
                rebateRecordInsert.setMerchantId(record.getMerchantId());
                rebateRecordInsert.setMerchantUid(record.getMerchantUid());
                rebateRecordInsert.setChanneler(record.getChanneler());
                rebateRecordInsert.setChannelerRebate(record.getChannelerRebate());
                rebateRecordInsert.setMerchantRebate(record.getMerchantRebate());
                rebateRecordInsert.setPlaceId(record.getPlaceId());
                rebateRecordInsert.setPlaceUid(record.getPlaceUid());
                rebateRecordInsert.setRebateTime(System.currentTimeMillis());
                rebateRecordInsert.setTenantId(record.getTenantId());
                rebateRecordInsert.setCreateTime(System.currentTimeMillis());
                rebateRecordInsert.setUpdateTime(System.currentTimeMillis());
                rebateRecordInsert.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_RETURNED);
                rebateRecordService.insert(rebateRecordInsert);
                
                //扣减商户、渠道商返利金额
                merchantUserAmountService.reduceAmount(record.getMerchantRebate(), record.getMerchantUid(), record.getTenantId().longValue());
                channelEmployeeAmountService.reduceAmount(record.getChannelerRebate(), record.getChanneler(), record.getTenantId().longValue());
            }
        }
    }
}
