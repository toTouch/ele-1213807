package com.xiliulou.electricity.constant;

public interface UserOperateRecordConstant {
    
    /**
     * 电池保险
     */
    Integer BATTERY_INSURANCE = 6;
    
    /**
     * 车辆保险
     */
    Integer CAR_INSURANCE = 7;
    
    /**
     * 编辑电池保险
     */
    Integer EDIT_BATTERY_INSURANCE_CONTENT = 13;
    
    /**
     * 续费电池保险
     */
    Integer RENEWAL_BATTERY_INSURANCE_CONTENT = 13;
    
    /**
     * 编辑车辆保险
     */
    Integer EDIT_CAR_INSURANCE_CONTENT = 14;
    
    /**
     * 续费车辆保险
     */
    Integer RENEWAL_CAR_INSURANCE_CONTENT = 15;
    
    /**
     * 0--正常  1--暂停
     */
    Integer CAR_MEMBER_CARD_ENABLE = 0;
    
    Integer CAR_MEMBER_CARD_DISABLE = 1;
    
    /**
     * 0--换电  1--租车
     */
    Integer OPERATE_TYPE_BATTERY=0;
    
    Integer OPERATE_TYPE_CAR=0;
}
