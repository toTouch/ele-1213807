package com.xiliulou.electricity.bo;

import lombok.Data;

/**
 * @ClassName: FreeDepositOrderStatusBO
 * @description:
 * @author: renhang
 * @create: 2024-08-22 11:15
 */
@Data
public class FreeDepositOrderStatusBO {
    
    /**
     * 商户订单号，免押时传的transId
     */
    private String transId;
    
    /**
     * 授权号
     */
    private String authNo;
    
    /**
     * 授权状态
     */
    private Integer authStatus;
}
