package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-29-17:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCarDepositVO {
    /**
     * 用户Id
     */
    private Long uid;
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
     * 门店id
     */
    private Long storeId;
    private String storeName;
    /**
     * 车辆型号Id
     */
    private Long carModelId;
    private String carModelName;
    /**
     * 交易方式 0--线上 1--线下
     */
    private Integer payType;
    
    /**
     * 车辆退押审核状态
     */
    private Integer returnDepositStatus;
    
    /**
     * 押金类型
     */
    private Integer depositType;
    
    /**
     * 是否有车辆  1--有  2--无
     */
    private Integer hasCarStatus;
    
    /**
     * 缴纳时间
     */
    private Long payTime;
    
    public static final Integer HAS_CAR_STATUS_YES = 1;
    
    public static final Integer HAS_CAR_STATUS_NO = 2;
}
