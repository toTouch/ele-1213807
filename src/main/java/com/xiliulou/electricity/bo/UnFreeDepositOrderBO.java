package com.xiliulou.electricity.bo;

import lombok.Data;

import javax.annotation.Nullable;

/**
 * @ClassName: UnFreeDepositOrderBO
 * @description:
 * @author: renhang
 * @create: 2024-08-22 17:50
 */
@Data
public class UnFreeDepositOrderBO {
    
    /**
     * 本次解冻金额，单位:分
     */
    @Nullable
    private Long transAmt;
    
    /**
     * 授权号
     */
    private String authNo;
    
    /**
     * 授权状态
     */
    @Nullable
    private Integer authStatus;
    
    /**
     * 11:解冻中;12:已解冻;
     */
    
    public static final Integer AUTH_UN_FREEZING = 11;
    
    public static final Integer AUTH_UN_FROZEN = 12;
}
