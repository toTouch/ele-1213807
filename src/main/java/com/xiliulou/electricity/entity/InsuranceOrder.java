package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 换电柜保险订单(InsuranceOrder)实体类
 *
 * @author makejava
 * @since 2022-11-03 14:44:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_insurance_order")
public class InsuranceOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 保险可用天数
     */
    private Integer validDays;

    private Long uid;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 支付状态 0--未支付 1--支付成功 2--支付失败
     */
    private Integer status;

    /**
     * 保险Id
     */
    private Integer insuranceId;

    /**
     * 保险名称
     */
    private String insuranceName;

    /**
     * 保险类型 0--电池 1--车辆
     */
    private Integer insuranceType;

    /**
     * 交易方式 0--线上 1--线下
     */
    private Integer payType;

    /**
     * 加盟商id
     */
    private Long franchiseeId;

    /**
     * 城市Id
     */
    private Integer cid;

    /**
     * 是否出险 0--未出险 1--已出险
     */
    private Integer isUse;

    //租户id
    private Integer tenantId;

    private Long createTime;

    private Long updateTime;

    public static final Integer BATTERY_INSURANCE_TYPE = 0;
    public static final Integer CAR_INSURANCE_TYPE = 1;

    public static final Integer NOT_USE = 0;
    public static final Integer IS_USE = 1;

    public static final Integer ONLINE_PAY_TYPE = 0;
    public static final Integer OFFLINE_PAY_TYPE = 1;

    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = 2;
}
