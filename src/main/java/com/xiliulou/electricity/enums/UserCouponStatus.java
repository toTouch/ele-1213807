package com.xiliulou.electricity.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * <p>
 * Description: This enum is UserCouponStatus!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/13
 **/
@Getter
@AllArgsConstructor
public enum UserCouponStatus {
    STATUS_UNUSED(1, "未使用"),
    STATUS_USED(2, "已使用"),
    STATUS_EXPIRED(3, "已过期"),
    STATUS_DESTRUCTION(4, "已核销"),
    STATUS_IS_BEING_VERIFICATION(5, "使用中"),
    STATUS_IS_INVALID(6, "已失效");
    
    private final Integer code;
    
    private final String desc;
    
    public static UserCouponStatus getUserCouponStatus(Integer code) {
        for (UserCouponStatus status : UserCouponStatus.values()) {
            if (Objects.equals(code,status.getCode())) {
                return status;
            }
        }
        return STATUS_IS_INVALID;
    }
}
