package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.DateFormatConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
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
import com.xiliulou.electricity.mq.model.MerchantModify;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.service.merchant.MerchantLevelService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.RebateConfigService;
import com.xiliulou.electricity.service.merchant.RebateRecordService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 商户升级后重新计算返利差额 计算返利差额，仅自动升级需要计算差额，
 * 后台手动修改不需要重新计算差额 原型需求：实时触发升级，升级到新等级后，当月的所有骑手均按新的返利规则返利；
 * 已结算的返利记录不变，需按新等级与升级前等级返利计算差额部分，添加“未结算”状态、“差额”类型的返利记录中；
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.MERCHANT_MODIFY_TOPIC, consumerGroup = MqConsumerConstant.MERCHANT_MODIFY_CONSUMER_GROUP, consumeThreadMax = 3)
public class MerchantModifyConsumer implements RocketMQListener<String> {
    
    @Autowired
    private RebateConfigService rebateConfigService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MerchantService merchantService;
    
    @Autowired
    private RebateRecordService rebateRecordService;
    
    @Autowired
    private MerchantLevelService merchantLevelService;
    
    @Autowired
    private MerchantAttrService merchantAttrService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private UserInfoExtraService userInfoExtraService;
    
    @Override
    public void onMessage(String message) {
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());
        
        log.info("MERCHANT MODIFY CONSUMER INFO!received msg={}", message);
        MerchantModify merchantModify = null;
        
        try {
            merchantModify = JsonUtil.fromJson(message, MerchantModify.class);
            
            if (Objects.isNull(merchantModify) || Objects.isNull(merchantModify.getMerchantId()) || Objects.isNull(merchantModify.getUid())) {
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
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(merchantModify.getUid());
            if (Objects.isNull(userInfo)) {
                log.warn("MERCHANT MODIFY CONSUMER WARN! userInfo is null, uid={}", merchantModify.getUid());
                return;
            }
            
            //当前商户等级
            String currentLevel = merchantLevel.getLevel();
            
            int offset = 0;
            int size = 200;
            
            long startTime = DateUtils.getDayStartTimeByLocalDate(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()));
            long endTime = DateUtils.getDayStartTimeByLocalDate(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())) + 24 * 60 * 60 * 1000L;
            
            while (true) {
                /**
                 * 获取商户本月上一级的返利记录，根据该条记录，生成差额
                 */
                List<RebateRecord> list = rebateRecordService.listCurrentMonthRebateRecord(merchant.getId(), startTime, endTime, offset, size);
                if (CollectionUtils.isEmpty(list)) {
                    return;
                }
                
                list.forEach(item -> {
                    //后台将商户等级修改至低等级后，再次自动升级时，排除已经计算过差额的返利记录
                    if (Integer.parseInt(item.getLevel()) <= Integer.parseInt(currentLevel)) {
                        log.info("MERCHANT MODIFY CONSUMER INFO!illegal level,id={},currentLevel={}", item.getId(), currentLevel);
                        return;
                    }
                    
                    //检查该订单是否生成已失效或已退回返利记录
                    if (Objects.nonNull(rebateRecordService.existsExpireRebateRecordByOriginalOrderId(item.getOriginalOrderId()))) {
                        return;
                    }
                    
                    MerchantAttr merchantAttr = merchantAttrService.queryByTenantId(userInfo.getTenantId());
                    if (Objects.isNull(merchantAttr)) {
                        log.warn("MERCHANT MODIFY CONSUMER WARN! merchantAttr is null, tenantId={}", userInfo.getTenantId());
                        return;
                    }
                    
                    Long channelEmployeeUid;
                    if (Objects.equals(merchantAttr.getStatus(), MerchantAttr.CLOSE_STATUS)) {
                        // 关闭按照之前逻辑，取商户表的渠道员
                        channelEmployeeUid = merchant.getChannelEmployeeUid();
                    } else {
                        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(item.getUid());
                        // 默认开启按照新逻辑
                        if (Objects.isNull(userInfoExtra)) {
                            log.warn("REBATE CONSUMER WARN! userInfoExtra is null ,uid={}", item.getUid());
                            return;
                        }
                        channelEmployeeUid = userInfoExtra.getChannelEmployeeUid();
                    }
                    log.info("MERCHANT MODIFY CONSUMER INFO! merchantAttr={},channelEmployeeUid={}", JsonUtil.toJson(merchantAttr), channelEmployeeUid);
                    
                    
                    //渠道员
                    User channel = userService.queryByUidFromCache(channelEmployeeUid);
                    //若商户及渠道员均禁用，不返利
                    if (Objects.equals(MerchantConstant.DISABLE, merchant.getStatus()) && Objects.nonNull(channel) && Objects.equals(channel.getLockFlag(), User.USER_LOCK)) {
                        log.warn("MERCHANT MODIFY CONSUMER WARN!merchant disable,channel disable,uid={},merchantId={}", item.getUid(), item.getMerchantId());
                        return;
                    }
                    
                    //获取最新返利规则
                    RebateConfig rebateConfig = rebateConfigService.queryByMidAndMerchantLevel(item.getMemberCardId(), currentLevel);
                    if (Objects.isNull(rebateConfig)) {
                        log.warn("MERCHANT MODIFY CONSUMER WARN!rebateConfig is null,id={},memberCardId={},level={}", item.getId(), item.getMemberCardId(), currentLevel);
                        return;
                    }
                    
                    if (Objects.equals(rebateConfig.getStatus(), MerchantConstant.REBATE_DISABLE)) {
                        log.warn("MERCHANT MODIFY CONSUMER WARN!rebateConfig is disable,id={},memberCardId={},level={}", item.getId(), item.getMemberCardId(), currentLevel);
                        return;
                    }
                  
                    if (Integer.parseInt(item.getLevel()) <= Integer.parseInt(currentLevel)) {
                        log.info("MERCHANT MODIFY CONSUMER INFO!illegal level,id={},itemLevel={},currentLevel={}", item.getId(), item.getLevel(), currentLevel);
                        return;
                    }
                    
                    BigDecimal newMerchantRebate = Objects.equals(item.getOrderType(), MerchantConstant.MERCHANT_REBATE_TYPE_INVITATION) ? rebateConfig.getMerchantInvitation()
                            : rebateConfig.getMerchantRenewal();
                    BigDecimal newChannelerRebate = Objects.equals(item.getOrderType(), MerchantConstant.MERCHANT_REBATE_TYPE_INVITATION) ? rebateConfig.getChannelerInvitation()
                            : rebateConfig.getChannelerRenewal();
                    
                    //上一级返利配置（兼容手动将商户级别修改低后，自动升级到更高级别差额的计算）
                    RebateConfig latestRebateConfig = rebateConfigService.queryLatestByMidAndMerchantLevel(item.getMemberCardId(), currentLevel);
                    if (Objects.isNull(latestRebateConfig)) {
                        log.warn("MERCHANT MODIFY CONSUMER WARN!latestRebateConfig is null,id={},memberCardId={},level={}", item.getId(), item.getMemberCardId(), currentLevel);
                        return;
                    }
                    
                    BigDecimal oldMerchantRebate =
                            Objects.equals(item.getOrderType(), MerchantConstant.MERCHANT_REBATE_TYPE_INVITATION) ? latestRebateConfig.getMerchantInvitation()
                                    : latestRebateConfig.getMerchantRenewal();
                    BigDecimal oldChannelerRebate =
                            Objects.equals(item.getOrderType(), MerchantConstant.MERCHANT_REBATE_TYPE_INVITATION) ? latestRebateConfig.getChannelerInvitation()
                                    : latestRebateConfig.getChannelerRenewal();
    
                    BigDecimal channelerRebate = newChannelerRebate.subtract(oldChannelerRebate);
                    channelerRebate = channelerRebate.compareTo(BigDecimal.ZERO) > 0 ? channelerRebate : BigDecimal.ZERO;
                    BigDecimal merchantRebate = newMerchantRebate.subtract(oldMerchantRebate);
                    merchantRebate = merchantRebate.compareTo(BigDecimal.ZERO) > 0 ? merchantRebate : BigDecimal.ZERO;
                    
                    log.info("MERCHANT MODIFY CONSUMER INFO!orderId={}", item.getOrderId());
                    
                    RebateRecord rebateRecord = new RebateRecord();
                    rebateRecord.setUid(item.getUid());
                    rebateRecord.setName(item.getName());
                    rebateRecord.setPhone(item.getPhone());
                    rebateRecord.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_REBATE, rebateRecord.getUid()));
                    rebateRecord.setOriginalOrderId(item.getOriginalOrderId());
                    rebateRecord.setMemberCardId(item.getMemberCardId());
                    rebateRecord.setMemberCardName(item.getMemberCardName());
                    rebateRecord.setType(MerchantConstant.MERCHANT_REBATE_TYPE_DISCREPANCY);
                    rebateRecord.setOrderType(item.getOrderType());
                    rebateRecord.setFranchiseeId(item.getFranchiseeId());
                    rebateRecord.setLevel(currentLevel);
                    rebateRecord.setMerchantId(item.getMerchantId());
                    rebateRecord.setMerchantUid(item.getMerchantUid());
                    rebateRecord.setStatus(MerchantConstant.MERCHANT_REBATE_STATUS_NOT_SETTLE);
                    //渠道员取实时的
                    rebateRecord.setChanneler(channelEmployeeUid);
                    rebateRecord.setChannelerRebate(channelerRebate);
                    rebateRecord.setMerchantRebate(merchantRebate);
                    rebateRecord.setPlaceId(item.getPlaceId());
                    rebateRecord.setPlaceUid(item.getPlaceUid());
                    rebateRecord.setRebateTime(System.currentTimeMillis());
                    rebateRecord.setTenantId(item.getTenantId());
                    rebateRecord.setCreateTime(System.currentTimeMillis());
                    rebateRecord.setUpdateTime(System.currentTimeMillis());
                    rebateRecord.setMonthDate(DateUtil.format(new Date(), DateFormatConstant.MONTH_DAY_DATE_FORMAT));
    
                    //商户禁用后，不给商户返利；渠道员禁用，不返利
                    if (Objects.equals(MerchantConstant.DISABLE, merchant.getStatus())) {
                        rebateRecord.setMerchantRebate(BigDecimal.ZERO);
                    }
                    
                    if (Objects.isNull(channel) || Objects.equals(channel.getLockFlag(), User.USER_LOCK) || Objects.equals(channel.getDelFlag(), User.DEL_DEL)) {
                        rebateRecord.setChannelerRebate(BigDecimal.ZERO);
                    }
    
                    //若渠道员与商户的返利差额都为0  则不生成返利差额记录
                    if (BigDecimal.ZERO.compareTo(channelerRebate) == 0 && BigDecimal.ZERO.compareTo(merchantRebate) == 0) {
                        log.info("MERCHANT MODIFY CONSUMER INFO!balance is zero,uid={}", item.getUid());
                        return;
                    }
                    
                    rebateRecordService.insert(rebateRecord);
                });
                
                offset += size;
            }
        } catch (Exception e) {
            log.error("MERCHANT MODIFY CONSUMER ERROR!msg={}", message, e);
        }
    }
}
