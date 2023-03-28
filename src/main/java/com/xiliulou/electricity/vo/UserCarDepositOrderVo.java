package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zgw
 * @date 2023/2/23 14:48
 * @mood
 */
@Data
public class UserCarDepositOrderVo {
    
    private String carModelName;
    
    private String orderId;
    
    private BigDecimal carDepositPay;
    
    private Long createTime;
    
    private Long refundTime;
}
