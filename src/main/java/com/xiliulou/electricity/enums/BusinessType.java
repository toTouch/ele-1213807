package com.xiliulou.electricity.enums;

/**
 * 业务类型枚举  业务类型定义 参考换电柜3.0需求文档 @王银
 */
public enum BusinessType {

    //换电订单
    EXCHANGE_BATTERY(10),
    //租电订单
    RENT_BATTERY(11),
    //退电订单
    RETURN_BATTERY(12),
    //换电套餐订单
    BATTERY_MEMBERCARD(13),
    //换电退租订单
    REFUND_BATTERY_MEMBERCARD(14),
    //电池押金订单
    BATTERY_DEPOSIT(15),
    //电池退押审核订单
    BATTERY_DEPOSIT_REFUND(16),


    //租车套餐订单
    CAR_MEMBERCARD(20),
    //租车退租金
    REFUND_CAR_MEMBERCARD(21),
    //租车押金
    CAR_DEPOSIT(22),
    //租车退押金
    CAR_DEPOSIT_REFUND(23),
    //还车订单
    RETURN_CAR(24),
    //租车订单
    RENT_CAR(25),


    //电池暂停套餐
    BATTERY_SUSPEND(30),
    //电池滞纳金订单
    BATTERY_STAGNATE(31),
    //车辆暂停套餐
    CAR_SUSPEND(32),
    //车辆滞纳金订单
    CAR_STAGNATE(33),


    //电池保险订单
    BATTERY_INSURANCE(40),
    //车辆保险订单
    CAR_INSURANCE(41),


    //免押订单
    FREE_DEPOSIT(50),
    //代扣订单
    WITHHOLD(51),


    //分帐订单
    DIVIDE_ACCOUNT(60),
    //提现订单
    WITHDRAW(61);

    private final Integer business;

    private BusinessType(Integer business) {
        this.business = business;
    }

    public Integer getBusiness() {
        return business;
    }
}
