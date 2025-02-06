package com.xiliulou.electricity.enums.batteryrecycle;

/**
 * 车辆运营数据查询类型
 */
public enum BatteryRecycleStatusEnum {

    INIT(0, "已录入"),
    LOCK(1, "已入柜锁定"),
    CANCEL(2, "已取消")
    ;

    private int code;

    private String desc;

    BatteryRecycleStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
