package com.xiliulou.electricity.enums.message;


import lombok.Getter;

/**
 * <p>
 * Description: This enum is RechargeAlarm!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/7/22
 **/
@Getter
public enum RechargeAlarm {
    
    SESAME_CREDIT(0, "芝麻信用"),
    FACIAL_VERIFICATION(1, "人脸审核"),
    ELECTRONIC_SIGNATURE(2, "电子签名"),
    AUTH_PAY(4, "分期签约次数");
    
    private final int code;
    
    private final String describe;
    
    RechargeAlarm(int code, String describe) {
        this.code = code;
        this.describe = describe;
    }
}
