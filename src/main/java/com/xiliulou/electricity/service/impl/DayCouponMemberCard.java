package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.DayCouponStrategy;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Description 换电套餐使用天数券
 * @Author: SongJP
 * @Date: 2024/11/14 9:23
 */
@Component
@RequiredArgsConstructor
public class DayCouponMemberCard implements DayCouponStrategy {
    
    private final UserBatteryMemberCardService userBatteryMemberCardService;
    
    private final BatteryMemberCardService batteryMemberCardService;
    
    
    @Override
    public boolean isLateFee(Long uid) {
        return false;
    }
    
    @Override
    public Pair<Boolean, Boolean> isFreezeOrAudit(Long uid) {
        return null;
    }
    
    @Override
    public boolean isOverdue(Long uid) {
        return false;
    }
    
    @Override
    public boolean isReturnTheDeposit(Long uid) {
        return false;
    }
    
    @Override
    public boolean isPackageInUse(Long uid) {
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            return false;
        }
        
        Long memberCardId = userBatteryMemberCard.getMemberCardId();
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(memberCardId);
        if (Objects.isNull(memberCardId) || Objects.equals(memberCardId, 0L) || userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            return false;
        }
        
        return !(Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0);
    }
    
    @Override
    public boolean process(Coupon coupon, Long uid) {
        return false;
    }
}
