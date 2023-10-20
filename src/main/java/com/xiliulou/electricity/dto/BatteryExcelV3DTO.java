package com.xiliulou.electricity.dto;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 导入电池excel 电池模型
 * @date 2023/10/20 15:55:11
 */
@Data
public class BatteryExcelV3DTO {
    /**
     * sn码
     */
    private String sn;
    
    /**
     * 电压
     */
    private Integer v;
    
    /**
     * 容量
     */
    Integer c;
}


