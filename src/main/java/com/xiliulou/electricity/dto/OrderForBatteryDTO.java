package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/15 18:13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderForBatteryDTO {
    
    private String orderIdForBattery;
    
    private Integer orderTypeForBattery;
}
