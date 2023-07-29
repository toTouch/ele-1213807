package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * (BatteryMembercardRefundOrder)实体类
 *
 * @author Eclair
 * @since 2023-07-12 15:56:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_battery_membercard_refund_order")
public class BatteryMembercardRefundOrder {
    /**
     * id
     */
    private Long id;

    private Long uid;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 套餐id
     */
    private Long mid;
    /**
     * 退款单号
     */
    private String refundOrderNo;
    /**
     * 套餐订单单号
     */
    private String memberCardOrderNo;
    /**
     * 支付金额,单位元
     */
    private BigDecimal payAmount;
    /**
     * 退款金额,单位元
     */
    private BigDecimal refundAmount;

    private Long remainingNumber;

    private Long remainingTime;
    /**
     * 订单状态
     */
    private Integer status;
    /**
     * 错误原因
     */
    private String msg;
    /**
     * 订单类型
     */
    private Integer type;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    private Integer tenantId;

    private Long franchiseeId;

    private Long storeId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //订单生成
    public static final Integer STATUS_INIT = 0;
    //后台同意退款
    public static final Integer STATUS_AGREE_REFUND = 1;
    //后台拒绝退款
    public static final Integer STATUS_REFUSE_REFUND = 2;
    //退款中
    public static final Integer STATUS_REFUND = 3;
    //退款成功
    public static final Integer STATUS_SUCCESS = 4;
    //退款失败
    public static final Integer STATUS_FAIL = 5;
}
