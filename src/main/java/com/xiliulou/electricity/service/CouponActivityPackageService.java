package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.CouponActivityPackage;
import com.xiliulou.electricity.enums.PackageTypeEnum;

import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/28 11:48
 * @Description:
 */
public interface CouponActivityPackageService {

    Integer addCouponActivityPackage(CouponActivityPackage couponActivityPackage);


    Integer addCouponActivityPackages(List<CouponActivityPackage> couponActivityPackageList);

    List<CouponActivityPackage> findCouponActivityPackages(CouponActivityPackage couponActivityPackage);

    List<CouponActivityPackage> findActivityPackagesByCouponId(Long couponId);

    List<CouponActivityPackage> findPackagesByCouponIdAndType(Long couponId, Integer packageType);

    /**
     * 检查集合中的优惠券是否针对当前套餐可用。有一个不满足条件，则检查结果为无效
     * @param coupons
     * @param packageId
     * @param packageType
     * @see PackageTypeEnum
     * @return
     */
    Boolean checkPackageIsValid(List<Coupon> coupons, Long packageId, Integer packageType);

}
