package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.core.base.enums.ChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 缴纳电池服务费订单表(tEleBatteryServiceFeeOrder)实体类
 *
 * @author makejava
 * @since 2022-04-19 10:16:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_battery_service_fee_order")
public class EleBatteryServiceFeeOrder {

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
    /**
     * 门店Id
     */
    private Long storeId;
    /**
     * 加盟商Id
     */
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
     * 电池sn码
     */
    private String sn;

    /**
     * 电池服务费单价
     */
    private BigDecimal batteryServiceFee;

    /**
     * 电池服务费产生时间
     */
    private Long batteryServiceFeeGenerateTime;

    /**
     * 电池服务费产生截止时间
     */
    private Long batteryServiceFeeEndTime;

    /**
     * 服务费来源 0--月卡过期 1--停卡
     */
    private Integer source;

    /**
     * 支付时间
     */
    private Long payTime;
    
    /**
     * 支付参数中的加盟商id
     */
    private Long paramFranchiseeId;
    
    /**
     * 微信商户号
     */
    private String wechatMerchantId;
    
    /**
     * 支付方式
     * @see ChannelEnum
     */
    private String paymentChannel;

    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = 2;
    public static final Integer STATUS_CLEAN = 3;

    public static final Integer MEMBER_CARD_OVERDUE = 0;
    public static final Integer DISABLE_MEMBER_CARD = 1;


}
