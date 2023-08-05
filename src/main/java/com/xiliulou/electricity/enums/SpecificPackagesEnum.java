package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: Kenneth
 * @Date: 2023/8/5 16:01
 * @Description:
 */

@Getter
@AllArgsConstructor
public enum SpecificPackagesEnum implements BasicEnum<Integer, String> {

    SPECIFIC_PACKAGES_YES(1, "是否指定套餐使用-是"),

    SPECIFIC_PACKAGES_NO(2, "是否指定套餐使用-否，适用于所有套餐"),

    ;

    private final Integer code;

    private final String desc;
}
