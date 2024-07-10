package com.xiliulou.electricity.dto;

import lombok.Data;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/7/9 9:28
 */
@Data
public class BatteryModelDTO {
    
    /**
     * 套餐id
     */
    private Long mid;
    
    /**
     * 电池短型号
     */
    private List<String> batteryModels;
}
