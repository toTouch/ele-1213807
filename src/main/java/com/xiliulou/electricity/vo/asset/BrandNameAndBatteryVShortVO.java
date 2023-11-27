package com.xiliulou.electricity.vo.asset;

import lombok.Data;

@Data
public class BrandNameAndBatteryVShortVO {
    
    private Long id;
    
    /**
     * 品牌
     */
    private String brandName;
    
    /**
     * 电池简称
     */
    private String batteryVShort;
    
    /**
     * 品牌型号（品牌/简称）
     */
    private String brandNameAndVShort;
    
    /**
     * 电池型号
     */
    private String batteryType;

}
