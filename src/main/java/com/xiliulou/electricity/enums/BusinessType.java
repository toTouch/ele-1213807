package com.xiliulou.electricity.enums;

/**
 * 业务类型枚举
 */
public enum BusinessType {
    /**
     * 10(电池套餐) 11（电池押金） 12（租车套餐） 13（租车押金） 14（电池保险）
     */
    
    BATTERY_PACKAGE(10),
    BATTERY_DEPOSIT(11),
    CAR_PACKAGE(12),
    CAR_DEPOSIT(13),
    CAR_INSURANCE(14);
    
    private final Integer business;
    
    private BusinessType(Integer business) {
        this.business = business;
    }
    
    public Integer getBusiness() {
        return business;
    }
}
