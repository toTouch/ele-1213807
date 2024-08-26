package com.xiliulou.electricity.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: PxzUnFreeDepositDTO
 * @description:
 * @author: renhang
 * @create: 2024-08-25 11:21
 */
@Data
@Builder
public class PxzUnFreeDepositDTO implements Serializable {
    
    /**
     * 免押订单
     */
    private String orderId;
    
    
    /**
     * 授权状态
     */
    private Integer authStatus;
    
    
}
