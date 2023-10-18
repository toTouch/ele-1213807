package com.xiliulou.electricity.vo.enterprise;

import com.xiliulou.electricity.enums.enterprise.CloudBeanStatusEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
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
     * 押金类型
     *  0-缴纳押金类型, 1-免押类型
     */
    private Integer depositType;
    
    /**
     * 用户电池型号
     */
    private String userBatterySimpleType;
    
    /**
     * 电池编码
     */
    private String batterySn;
    

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
    
    private Long paymentTime;
    
    private Long packageExpiredTime;
    
    private Long effectiveTime;
    
    /**
     * 自主续费状态 0:不自主续费, 1:自主续费
     * @see RenewalStatusEnum
     */
    private Integer renewalStatus;
    
    /**
     * 云豆状态（1-未回收，2-已回收）
     * @see CloudBeanStatusEnum
     */
    private Integer cloudBeanStatus;
    
    //private BigDecimal canRecycleBeanAmount;
    
    //private BigDecimal recycledBeanAmount;
    
    //private Integer expirationDays;
    
    
    
}
