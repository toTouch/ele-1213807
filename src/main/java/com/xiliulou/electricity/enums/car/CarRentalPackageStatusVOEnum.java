package com.xiliulou.electricity.enums.car;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author HeYafeng
 * @description 页面展示用的状态
 * @date 2025/1/3 20:30:30
 */
@Getter
@AllArgsConstructor
public enum CarRentalPackageStatusVOEnum implements BasicEnum<Integer, String> {
    
    CAR_PACKAGE_FROZEN(0, "已冻结"),
    CAR_PACKAGE_UN_FROZEN(1, "未冻结"),
    ;
    
    private final Integer code;
    
    private final String desc;
}