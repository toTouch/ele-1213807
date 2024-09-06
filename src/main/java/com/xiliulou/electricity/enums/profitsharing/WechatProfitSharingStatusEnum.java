package com.xiliulou.electricity.enums.profitsharing;

/**
 * @author maxiaodong
 * @date 2024/9/6 13:50
 * @desc
 */
public enum WechatProfitSharingStatusEnum {
    PROCESSING( "PROCESSING"),
    FINISHED("FINISHED"),
    PENDING( "PENDING"),
    SUCCESS("SUCCESS"),
    CLOSED("CLOSED"),;
    
    private String desc;
    
    WechatProfitSharingStatusEnum(String desc) {
        this.desc = desc;
    }
    
    public String getDesc() {
        return desc;
    }
}
