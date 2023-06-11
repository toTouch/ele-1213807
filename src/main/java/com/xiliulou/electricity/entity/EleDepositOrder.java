package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 缴纳押金订单表(TEleDepositOrder)实体类
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_deposit_order")
public class EleDepositOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
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
     */
    private Integer modelType;

    /**
     * 电池类型
     */
    private String batteryType;

    /**
     * 押金类型
     */
    private Integer depositType;

    /**
     * 门店Id
     */
    private Long storeId;

    /**
     * 车辆型号Id
     */
    private Integer carModelId;

    /**
     * 交易方式
     */
    private Integer payType;

    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = 2;

    public static final Integer ELECTRICITY_DEPOSIT = 0;
    public static final Integer RENT_CAR_DEPOSIT = 1;

    public static final Integer ONLINE_PAYMENT = 0;
    public static final Integer OFFLINE_PAYMENT = 1;
    
    /**
     * 免押
     */
    public static final Integer ONLINE_DEPOSIT_PAYMENT = 0;
    
    public static final Integer OFFLINE_DEPOSIT_PAYMENT = 1;
    public static final Integer FREE_DEPOSIT_PAYMENT = 2;


}
