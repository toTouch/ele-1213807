package com.xiliulou.electricity.service;

/**
 * @Description: CouponPackageService
 * @Author: renhang
 * @Date: 2025/01/16
 */

public interface CouponPackageItemService {

    /**
     * 是否存在优惠券绑定的优惠券包
     *
     * @param couponId couponId
     * @return: Integer
     */

    Integer existsCouponBindPackage(Long couponId);

}
