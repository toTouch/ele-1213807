package com.xiliulou.electricity.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @date 2025/2/25 15:41:16
 */
@Data
public class UserBatteryDepositBO {
    
    private Long id;
    
    /**
     * uid
     */
    private Long uid;
    
    /**
     * 交押金 对应的套餐id
     */
    private Long did;
    
    /**
     * 押金订单编号
     */
    private String orderId;
    
    /**
     * 押金金额
     */
    private BigDecimal batteryDeposit;
    
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
    
    private Long createTime;
    
    private Long updateTime;
    
    /**
     * 押金类型
     */
    private Integer depositType;
    
    /**
     * 缴纳押金的时间
     */
    private Long applyDepositTime;
    
    private Integer depositModifyFlag;
    
    private BigDecimal beforeModifyDeposit;
    
    /**
     * EleDepositOrder：易方式 0 线上，1线下，2免押，3美团支付
     */
    private Integer payType;
}
