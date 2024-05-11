package com.xiliulou.electricity.enums;


/**
 * <p>
 * Description: This enum is OverdueType!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/5/11
 **/
public enum OverdueType {
    BATTERY(0),
    CAR(1);
    
    private final Integer code;
    
    OverdueType(Integer code) {
        this.code = code;
    }
    
    public Integer getCode() {
        return code;
    }
}
