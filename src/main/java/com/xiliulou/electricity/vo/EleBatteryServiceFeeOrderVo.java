package com.xiliulou.electricity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
public class EleBatteryServiceFeeOrderVo {

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
     * */
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
     * 加盟商名字
     */
    private String franchiseeName;

    /**
     * 电池服务费
     */
    private BigDecimal batteryServiceFee;

    /**
     *电池类型
     */
    private Integer model;

    //新分型号押金
    private String modelBatteryDeposit;

    /**
     * 电池服务费产生时间
     */
    private Long batteryServiceFeeGenerateTime;

    /**
     * 服务费计费天数
     */
    private Integer batteryGenerateDay;

    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = 2;


}
