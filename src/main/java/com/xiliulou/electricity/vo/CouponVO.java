package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.Coupon;
import lombok.Data;

import java.util.List;

/**
 * @author: Miss.Li
 * @Date: 2021/7/15 10:03
 * @Description:
 */
@Data
public class CouponVO {

    //已领取
    public static final Integer IS_RECEIVED = 1;

    //未领取
    public static final Integer IS_NOT_RECEIVE = 2;

    //不能领取
    public static final Integer IS_CANNOT_RECEIVE = 3;

    /**
     * 邀请人数
     */
    private Integer triggerCount;

    /**
     * 优惠券
     */
    private Coupon coupon;
    
    /**
     * 优惠券列表
     */
    private List<Coupon> couponArrays;

    /**
     * 是否领取 1--已领取  2--未领取  3--不能领取
     */
    private Integer isGet;
    
    
    
    
    /**
     * 优惠券的换电套餐列表
     */
    private List<CouponMemberCardVO> batteryCouponCards;
    
    /**
     * 优惠券的租车套餐列表
     */
    private List<CouponMemberCardVO> carRentalCouponCards;
    
    /**
     * 优惠券的车电一体套餐列表
     */
    private List<CouponMemberCardVO> carWithBatteryCouponCards;
    
}
