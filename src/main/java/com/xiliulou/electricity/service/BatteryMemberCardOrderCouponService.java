package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryMemberCardOrderCoupon;

import java.util.List;

/**
 * (BatteryMemberCardOrderCoupon)表服务接口
 *
 * @author zzlong
 * @since 2023-06-02 14:52:19
 */
public interface BatteryMemberCardOrderCouponService {


    List<Long> selectCouponIdsByOrderId(String orderId);


    String selectOrderIdByCouponId(Long couponId);


    Integer batchInsert(List<BatteryMemberCardOrderCoupon> list);

}
