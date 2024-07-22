package com.xiliulou.electricity.enums.message;

import lombok.Getter;

/**
 * <p>
 * Description: This enum is SiteMessageType!
 * </p>
 * <p>Project: xiliulou-site-message</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/7/11
 **/
@Getter
public enum SiteMessageType {
    //消息通知
    REAL_NAME_VERIFICATION("REAL_NAME_VERIFICATION", "实名认证"),
    EXCHANGE_BATTERY_AND_RETURN_THE_DEPOSIT("EXCHANGE_BATTERY_AND_RETURN_THE_DEPOSIT", "换电退还押金"),
    BATTERY_SWAPPING_FREEZE("BATTERY_SWAPPING_FREEZE", "换电冻结套餐审核"),
    REPLACING_THE_BATTERY_AND_TERMINATING_THE_LEASE("REPLACING_THE_BATTERY_AND_TERMINATING_THE_LEASE", "换电租金退款审核"),
    CAR_RENTAL_REFUND("CAR_RENTAL_REFUND", "租车退押审核"),
    CAR_RENTAL_FREEZE("CAR_RENTAL_FREEZE", "租车冻结套餐审核"),
    CAR_RENTAL_AND_TERMINATION("CAR_RENTAL_AND_TERMINATION", "租车租金退款审核"),
    MERCHANT_WITHDRAWAL("MERCHANT_WITHDRAWAL", "商户提现"),
    
    //告警
    SMOKE_DETECTOR_ALARM("02017001", "烟感告警"),
    WATER_INTRUSION_ALARM("02018001", "水浸告警"),
    HIGH_TEMPERATURE_ALARM("02009001", "格口高温"),
    REAR_DOOR_OPEN("02099001", "后门开启"),
    RELAY_ADHESION("02097001", "继电器粘连"),
    CABINET_DOOR_FIRE_EXTINGUISHER_ALARM("02005001", "柜门灭火器告警"),
    HIGH_TEMPERATURE_POWER_SUPPLY_ALARM("02029001", "柜控电源告警：电源高温"),
    BATTERY_ABNORMALITY_ALARM("02096001", "电池异常告警"),
    CABINET_MACHINE_FULL_STORAGE_ALARM("02019003", "柜机满仓告警"),
    CABINET_MACHINE_MULTI_POWER_ALARM("02019004", "柜机多电告警"),
    CABINET_MACHINE_LOW_POWER_ALARM("02019002", "柜机少电告警"),
    ABNORMAL_DISAPPEARANCE_OF_BATTERY_POWER_OUTAGE_ALARM("02095001", "电池异常消失-断电"),
    ABNORMAL_BATTERY_OCCURRENCE_ALARM("02094001", "电池异常出现-断电"),
    //其他
    INSUFFICIENT_RECHARGE_BALANCE("INSUFFICIENT_RECHARGE_BALANCE", "充值余额不足"),
    UNKNOWN("UNKNOWN", "未知");
    
    private final String code;
    private final String describe;
    
    SiteMessageType(String code, String describe) {
        this.code = code;
        this.describe = describe;
    }
}
