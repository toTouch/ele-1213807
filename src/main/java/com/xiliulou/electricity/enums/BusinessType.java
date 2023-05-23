package com.xiliulou.electricity.enums;

/**
 * 业务类型枚举
 */
public enum BusinessType {
    /**
     * 10(电池套餐) 11（电池交押金） 12（租车套餐） 13（租车交押金） 14（电池保险） 15（电池退押金） 16（租车退押金） 17(租车)
     * 18(免押), 19(还车申请)  ,21(租电池), 22(还电池)
     */
    
    BATTERY_PACKAGE(10),
    BATTERY_DEPOSIT(11),
    CAR_PACKAGE(12),
    CAR_DEPOSIT(13),
    BATTERY_INSURANCE(14),
    BATTERY_REFUND(15),
    CAR_REFUND(16),
    RENT_CAR(17),
    FREE_DEPOSIT(18),
    RETURN_CAR(20),
    RENT_BATTERY(21),
    RETURN_BATTERY(22);

    private final Integer business;
    
    private BusinessType(Integer business) {
        this.business = business;
    }
    
    public Integer getBusiness() {
        return business;
    }
}
