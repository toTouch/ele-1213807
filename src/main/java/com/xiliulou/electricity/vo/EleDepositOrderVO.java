package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Hardy
 * @date 2021/12/3 17:37
 * @mood
 */
@Data
public class EleDepositOrderVO {
    private Long id;
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
    /**
     * 用户Id
     */
    private Long uid;
    /**
     * 订单Id
     */
    private String orderId;
    /**
     * 状态（0、未支付,1、支付成功,2、支付失败）
     */
    private Integer status;
    /**
     * 用户名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;

    //租户id
    private Integer tenantId;

    private Long franchiseeId;

    /**
     * 加盟商类型 1--老（不分型号） 2--新（分型号）
     * */
    private Integer modelType;

    /**
     * 电池类型
     */
    private String batteryType;

    private String franchiseeName;
}
