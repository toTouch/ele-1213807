package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-28-15:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarDepositOrderVO {
    private Long id;
    /**
     * 订单Id
     */
    private String orderId;
    /**
     * 用户名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    /**
     * 状态（0、未支付,1、支付成功,2、支付失败）
     */
    private Integer status;
    /**
     * 门店
     */
    private String storeName;
    /**
     * 车辆型号
     */
    private String carModelName;
    /**
     * 交易方式 0--线上 1--线下
     */
    private Integer payType;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 车辆编号
     */
    private String carSn;

    /**
     * 是否租电池
     */
    private Integer rentBattery;
    /**
     * 租赁方式
     */
    private String rentType;
}
