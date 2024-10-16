package com.xiliulou.electricity.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/15 18:13
 */
@Data
@Builder
public class OrderForBatteryDTO {
    
    private String orderId;
    
    private Integer orderType;
}
