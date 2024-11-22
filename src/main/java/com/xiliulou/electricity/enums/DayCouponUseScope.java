package com.xiliulou.electricity.enums;


import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * Description: This enum is DayCouponUseScope!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/14
 **/
@Getter
@RequiredArgsConstructor
public enum DayCouponUseScope {
    
    CAR(0, "租车"),
    BATTERY(1, "租电"),
    BOTH(2, "车电一体"),
    UNKNOWN(-1, "全部/未知");
    
    private final Integer code;
    private final String desc;
    
    public static DayCouponUseScope getByCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (DayCouponUseScope value : DayCouponUseScope.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
