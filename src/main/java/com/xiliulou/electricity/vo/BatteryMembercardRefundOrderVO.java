package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-12-15:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryMembercardRefundOrderVO {

    private Long id;

    private Long uid;

    private String name;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 套餐id
     */
    private Long mid;
    private String memberCardName;
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
    private Integer payType;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 0:不限制,1:限制
     */
    private Integer limitCount;

    /**
     * 租期单位 0：分钟，1：天
     */
    private Integer rentUnit;



}
