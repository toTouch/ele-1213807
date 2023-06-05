package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (BatteryMemberCardOrderCoupon)实体类
 *
 * @author Eclair
 * @since 2023-06-02 14:52:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_battery_member_card_order_coupon")
public class BatteryMemberCardOrderCoupon {

    private Long id;
    /**
     * 换电套餐订单号
     */
    private String orderId;
    /**
     * 优惠券Id
     */
    private Long couponId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    /**
     * 租户id
     */
    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
