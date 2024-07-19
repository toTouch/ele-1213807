package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: ExchangeReasonCellDTO
 * @description:
 * @author: renhang
 * @create: 2024-07-19 14:16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeReasonCellDTO {
    
    private Integer newCell;
    
    private Integer oldCell;
    
    private String msg;
}
