package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName: FyFreeDepositDelayDTO
 * @description:
 * @author: renhang
 * @create: 2024-08-26 11:00
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FyFreeDepositDelayDTO implements Serializable {
    
    private String orderId;
}
