package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.UserCoupon;
import lombok.Data;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-02-17:07
 */
@Data
public class MemberCardOrderCouponDTO {

    /**
     * 套餐订单
     */
    private ElectricityMemberCardOrder memberCardOrder;
    /**
     * 用户优惠券
     */
    private List<UserCoupon> userCoupons;

    /**
     *
     */
}
