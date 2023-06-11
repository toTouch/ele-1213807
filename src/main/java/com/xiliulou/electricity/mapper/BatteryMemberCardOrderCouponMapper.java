package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.BatteryMemberCardOrderCoupon;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (BatteryMemberCardOrderCoupon)表数据库访问层
 *
 * @author zzlong
 * @since 2023-06-02 14:52:19
 */
public interface BatteryMemberCardOrderCouponMapper extends BaseMapper<BatteryMemberCardOrderCoupon> {

    Integer batchInsert(List<BatteryMemberCardOrderCoupon> list);

    List<Long> selectCouponIdsByOrderId(@Param("orderId") String orderId);

    String selectOrderIdByCouponId(@Param("couponId") Long couponId);
}
