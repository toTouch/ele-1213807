package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.CouponPackage;
import com.xiliulou.electricity.query.CouponPackagePageQuery;

import java.util.List;

/**
 * @Description: CouponPackageMapper
 * @Author: renhang
 * @Date: 2025/01/16
 */

public interface CouponPackageMapper {

    void saveCouponPackage(CouponPackage couponPackage);

    CouponPackage selectCouponPackageById(Long id);

    void updateCouponPackage(CouponPackage updateCouponPackage);

    void deleteCouponPackageById(Long packageId);

    List<CouponPackage> selectPageCouponPackage(CouponPackagePageQuery query);

    Integer selectCountCouponPackage(CouponPackagePageQuery query);
}
