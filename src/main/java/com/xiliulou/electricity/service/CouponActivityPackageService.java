package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.CouponActivityPackage;

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

}
