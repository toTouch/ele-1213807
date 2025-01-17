package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : renhang
 * @description CouponPackageItems
 * @date : 2025-01-16 15:01
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_coupon_package_item")
public class CouponPackageItem {
    /**
     * Id
     */
    private Long id;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券包ID
     */
    private Long packageId;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠类型，1:减免券，3:天数劵
     */
    private Integer discountType;

    /**
     * 优惠折扣
     */
    private Double discount;

    /**
     * 优惠券作用(冗余)
     */
    private String effect;

    /**
     * 有效天数(冗余避免下发每次查询优惠券，如果以后优惠券可以编辑，这里需要一起更新)
     */
    private Integer days;

    /**
     * 优惠券数量
     */
    private Integer count;

    /**
     * 是否可叠加 0：否，1：是
     */
    private Integer superposition;

    /**
     * 0:正常 1:删除
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
}
