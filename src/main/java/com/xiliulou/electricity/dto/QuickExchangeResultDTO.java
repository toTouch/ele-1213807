package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: QuickExchangeResultDTO
 * @description: 快捷换电命令下发结果
 * @author: renhang
 * @create: 2024-11-22 17:19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuickExchangeResultDTO {
    
    private Boolean success;
    
    private String msg;
}
