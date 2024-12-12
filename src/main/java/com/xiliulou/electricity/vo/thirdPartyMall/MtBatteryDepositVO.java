package com.xiliulou.electricity.vo.thirdPartyMall;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 电池押金信息
 * @date 2024/8/29 14:26:38
 */
@Data
public class MtBatteryDepositVO {
    
    private Long uid;
    
    private Long franchiseeId;
    
    /**
     * 电池押金状态 0--未缴纳押金，1--已缴纳押金
     */
    private Integer batteryDepositStatus;
    
    private BigDecimal batteryDeposit;
    
    /**
     * 交易方式 0 线上，1线下，2免押，3美团支付
     */
    private Integer batteryDepositPayType;
    
    /**
     * 是否免押 0--是 1--否
     */
    private Integer freeDeposit;
    
    /**
     * 套餐ID
     */
    private Long packageId;
    
    /**
     * 退押状态
     */
    private Integer refundStatus;
    
}
