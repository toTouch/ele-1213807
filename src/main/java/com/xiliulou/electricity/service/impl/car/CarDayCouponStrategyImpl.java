package com.xiliulou.electricity.service.impl.car;


import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.enums.DayCouponUseScope;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.service.DayCouponStrategy;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: This class is DayCouponStrategyImpl!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/14
 **/
@Slf4j
@Service("carDayCouponStrategy")
@RequiredArgsConstructor
public class CarDayCouponStrategyImpl implements DayCouponStrategy {
    
    private final CarRentalPackageMemberTermService carRentalPackageMemberTermService;
    
    private final CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;
    
    private final UserInfoService userInfoService;
    
    @Override
    public DayCouponUseScope getScope(Integer tenantId, Long uid) {
        CarRentalPackageMemberTermPo memberTermPo = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (Objects.nonNull(memberTermPo) && MemberTermStatusEnum.NORMAL.getCode().equals(memberTermPo.getStatus())) {
            if (Objects.equals(memberTermPo.getRentalPackageType(), RentalPackageTypeEnum.CAR_BATTERY.getCode())) {
                return DayCouponUseScope.BOTH;
            }
            if (Objects.equals(memberTermPo.getRentalPackageType(), RentalPackageTypeEnum.CAR.getCode())) {
                return DayCouponUseScope.CAR;
            }
        }
        return DayCouponUseScope.UNKNOWN;
    }
    
    @Override
    public boolean isLateFee(Integer tenantId, Long uid) {
        BigDecimal bigDecimal = carRenalPackageSlippageBizService.queryCarPackageUnpaidAmountByUid(tenantId, uid);
        return Objects.nonNull(bigDecimal) && bigDecimal.compareTo(BigDecimal.ZERO) > 0;
    }
    
    @Override
    public Pair<Boolean, Boolean> isFreezeOrAudit(Integer tenantId, Long uid) {
        CarRentalPackageMemberTermPo memberTermPo = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (Objects.nonNull(memberTermPo)) {
            return Pair.of(MemberTermStatusEnum.FREEZE.getCode().equals(memberTermPo.getStatus()), MemberTermStatusEnum.APPLY_FREEZE.getCode().equals(memberTermPo.getStatus()));
        }
        return null;
    }
    
    @Override
    public boolean isOverdue(Integer tenantId, Long uid) {
        CarRentalPackageMemberTermPo memberTermPo = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (Objects.nonNull(memberTermPo) && MemberTermStatusEnum.NORMAL.getCode().equals(memberTermPo.getStatus()) && Objects.nonNull(memberTermPo.getDueTimeTotal())) {
            return System.currentTimeMillis() >= memberTermPo.getDueTimeTotal();
        }
        return false;
    }
    
    @Override
    public boolean isReturnThePackage(Integer tenantId, Long uid) {
        CarRentalPackageMemberTermPo memberTermPo = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        return Objects.nonNull(memberTermPo) && MemberTermStatusEnum.APPLY_RENT_REFUND.getCode().equals(memberTermPo.getStatus());
    }
    
    @Override
    public Pair<Boolean, Boolean> isReturnTheDeposit(Integer tenantId, Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        Boolean left =
                Objects.isNull(userInfo) || (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_NO) && Objects.equals(userInfo.getCarBatteryDepositStatus(),
                        YesNoEnum.NO.getCode()));
        
        CarRentalPackageMemberTermPo memberTermPo = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        Boolean right = Objects.nonNull(memberTermPo) && MemberTermStatusEnum.APPLY_REFUND_DEPOSIT.getCode().equals(memberTermPo.getStatus());
        
        return Pair.of(left, right);
    }
    
    @Override
    public boolean isPackageInUse(Integer tenantId, Long uid) {
        CarRentalPackageMemberTermPo memberTermPo = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        return Objects.nonNull(memberTermPo) && MemberTermStatusEnum.NORMAL.getCode().equals(memberTermPo.getStatus()) && Objects.nonNull(memberTermPo.getDueTime());
    }
    
    @Override
    public Triple<Boolean, Long, String> process(Coupon coupon, Integer tenantId, Long uid) {
        CarRentalPackageMemberTermPo memberTermPo = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (Objects.isNull(memberTermPo)) {
            return Triple.of(false, null, null);
        }
        Integer day = coupon.getCount();
        long millis = TimeUnit.DAYS.toMillis(day);
        Long dueTimeTotal = memberTermPo.getDueTimeTotal();
        Long dueTime = memberTermPo.getDueTime();
        CarRentalPackageMemberTermPo update = new CarRentalPackageMemberTermPo();
        update.setId(memberTermPo.getId());
        if (Objects.nonNull(dueTime)) {
            update.setDueTime(millis + dueTime);
        }
        if (Objects.nonNull(dueTimeTotal)) {
            update.setDueTimeTotal(millis + dueTimeTotal);
        }
        update.setUpdateTime(System.currentTimeMillis());
        update.setRemark("使用天数券");
        carRentalPackageMemberTermService.updateById(update);
        return Triple.of(true, memberTermPo.getRentalPackageId(), memberTermPo.getRentalPackageOrderNo());
    }
}
