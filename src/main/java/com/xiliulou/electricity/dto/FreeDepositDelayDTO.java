package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName: FreeDepositDelayDTO
 * @description:
 * @author: renhang
 * @create: 2024-08-26 11:00
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FreeDepositDelayDTO implements Serializable {
    
    /**
     * 免押渠道
     */
    private Integer channel;
    
    /**
     * 免押订单号
     */
    private String orderId;
}
