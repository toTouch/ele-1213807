package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.CouponPackage;

/**
 * @Description: CouponPackageMapper
 * @Author: renhang
 * @Date: 2025/01/16
 */

public interface CouponPackageMapper {

    void saveCouponPackage(CouponPackage couponPackage);

    CouponPackage selectCouponPackageById(Long id);
}
