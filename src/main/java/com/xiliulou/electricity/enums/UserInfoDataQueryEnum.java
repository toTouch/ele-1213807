package com.xiliulou.electricity.enums;

public enum UserInfoDataQueryEnum {

    ALL(0, "全部"),

    EFFECTIVE_USER(1,"有效用户"),

    RENT_USER(2,"在租用户"),

    OVERDUE_USER(3,"逾期用户"),

    SILENT_USER(4,"静默用户"),
    ;

    private int code;

    private String desc;

    UserInfoDataQueryEnum(int code, String desc) {
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
