package com.xiliulou.electricity.service.impl.car;


import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.service.DayCouponStrategy;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

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
    
    @Override
    public boolean isLateFee(Integer tenantId, Long uid) {
        return false;
    }
    
    @Override
    public Pair<Boolean, Boolean> isFreezeOrAudit(Integer tenantId, Long uid) {
        return null;
    }
    
    @Override
    public boolean isOverdue(Integer tenantId, Long uid) {
        return false;
    }
    
    @Override
    public boolean isReturnTheDeposit(Integer tenantId, Long uid) {
        return false;
    }
    
    @Override
    public boolean isPackageInUse(Integer tenantId, Long uid) {
        return false;
    }
    
    @Override
    public boolean process(Coupon coupon, Integer tenantId, Long uid) {
        return false;
    }
}
