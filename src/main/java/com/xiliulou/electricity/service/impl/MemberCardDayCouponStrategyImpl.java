package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.enums.DayCouponUseScope;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.DayCouponStrategy;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @Description 换电套餐使用天数券
 * @Author: SongJP
 * @Date: 2024/11/14 9:23
 */
@Slf4j
@Component("MemberCardDayCouponStrategyImpl")
@RequiredArgsConstructor
public class MemberCardDayCouponStrategyImpl implements DayCouponStrategy {
    
    private final UserBatteryMemberCardService userBatteryMemberCardService;
    
    private final BatteryMemberCardService batteryMemberCardService;
    
    private final ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    private final UserBatteryDepositService userBatteryDepositService;
    
    private final EleRefundOrderService eleRefundOrderService;
    
    private final ElectricityMemberCardOrderService electricityMemberCardOrderService;
    

    @Override
    public DayCouponUseScope getScope(Integer tenantId, Long uid) {
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard) || Objects.equals(userBatteryMemberCard.getMemberCardId(), 0L)) {
            return DayCouponUseScope.UNKNOWN;
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            return DayCouponUseScope.UNKNOWN;
        }
        
        // 除了车电一体其他的套餐的处理逻辑一致，都归属到租电类型下
        switch (batteryMemberCard.getBusinessType()) {
            case 0:
            case 2:
            case 3:
            case 4:
                return DayCouponUseScope.BATTERY;
            case 1:
                return DayCouponUseScope.BOTH;
            default:
                return DayCouponUseScope.UNKNOWN;
        }
    }

    @Override
    public boolean isLateFee(Integer tenantId, Long uid) {
        // 计算金额不为0表示有滞纳金
        EleBatteryServiceFeeVO eleBatteryServiceFeeVO = serviceFeeUserInfoService.queryUserBatteryServiceFee(uid);
        return !Objects.isNull(eleBatteryServiceFeeVO) && !Objects.isNull(eleBatteryServiceFeeVO.getBatteryServiceFee())
                && eleBatteryServiceFeeVO.getBatteryServiceFee().compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public Pair<Boolean, Boolean> isFreezeOrAudit(Integer tenantId, Long uid) {
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        Boolean left = Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) ? Boolean.TRUE : Boolean.FALSE;
        Boolean right = Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW) ? Boolean.TRUE : Boolean.FALSE;
        return Pair.of(left, right);
    }

    @Override
    public boolean isOverdue(Integer tenantId, Long uid) {
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard) || userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            return true;
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            return true;
        }

        return Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0;
    }

    @Override
    public boolean isReturnTheDeposit(Integer tenantId, Long uid) {
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryDeposit)) {
            return true;
        }
        
        // 是否有正在进行中的退押
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
        return refundCount > 0;
    }
    
    @Override
    public boolean isPackageInUse(Integer tenantId, Long uid) {
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            log.info("DAY COUPON INFO! There is no userBatteryMemberCard. uid={}", uid);
            return false;
        }
        
        Long memberCardId = userBatteryMemberCard.getMemberCardId();
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(memberCardId);
        if (Objects.isNull(memberCardId) || Objects.equals(memberCardId, 0L) || userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            log.info("DAY COUPON INFO! There is no batteryMemberCard or the batteryMemberCard is expired. uid={}", uid);
            return false;
        }
        
        if ((Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0)) {
            log.info("DAY COUPON INFO! The number of limited batteryMemberCard has been used up. uid={}", uid);
            return false;
        }
        
        return true;
    }
    
    @Override
    public Triple<Boolean,Long ,String> process(Coupon coupon, Integer tenantId, Long uid) {
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        
        ElectricityMemberCardOrder memberCardOrder = electricityMemberCardOrderService.selectByOrderNo(userBatteryMemberCard.getOrderId());
        if (Objects.isNull(memberCardOrder)) {
            log.warn("DAY COUPON WARN! day coupon order does not exist");
            return Triple.of(Boolean.FALSE, null, null);
        }
        
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(uid);
        userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() + coupon.getCount() * 24 * 60 * 60 * 1000L);
        userBatteryMemberCardUpdate.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime() + coupon.getCount() * 24 * 60 * 60 * 1000L);
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        
        ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
        serviceFeeUserInfoUpdate.setUid(uid);
        serviceFeeUserInfoUpdate.setTenantId(tenantId);
        serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCardUpdate.getMemberCardExpireTime());
        serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());

        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
        
        return Triple.of(Boolean.TRUE, userBatteryMemberCard.getMemberCardId(), memberCardOrder.getOrderId());
    }
}
