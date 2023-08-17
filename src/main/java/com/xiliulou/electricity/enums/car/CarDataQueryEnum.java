package com.xiliulou.electricity.enums.car;

/**
 * 车辆运营数据查询类型
 */
public enum CarDataQueryEnum {

    ALL(0, "全部"),

    RENT(1, "已租车辆"),

    NOT_RENT(2,"待租车辆"),

    OVERDUE(3,"逾期车辆"),

    OFFLINE(4,"离线车辆"),
    ;

    private int code;

    private String desc;

    CarDataQueryEnum(int code, String desc) {
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
