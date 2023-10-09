package com.xiliulou.electricity.vo.enterprise;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/26 10:21
 */

@Data
public class EnterprisePackageOrderVO {
    
    private Long enterpriseId;

    private Long uid;

    private String name;

    private String phone;
    
    /**
     * 套餐支付金额
     */
    private BigDecimal payAmount;
    
    /**
     * 押金金额
     */
    private BigDecimal batteryDeposit;
    
    /**
     * 用户电池型号
     */
    private String userBatterySimpleType;
    
    /**
     * 可回收云豆数
     */
    private BigDecimal recyclableBeanAmount;

    private String orderNo;
    
    /**
     * 套餐ID
     */
    private Long packageId;
    
    /**
     * 套餐名称
     */
    private String packageName;

    private Long createTime;

    private Long updateTime;
    
    private Long packageExpiredTime;
    
    private Long effectiveTime;
    
    //private Integer expirationDays;
    
    
    
}
