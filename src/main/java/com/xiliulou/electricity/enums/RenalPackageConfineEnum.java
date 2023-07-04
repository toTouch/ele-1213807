package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 租赁套餐限制枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum RenalPackageConfineEnum implements BasicEnum<Integer, String> {

    NO(1, "不限制"),
    NUMBER(2, "次数"),
    ;

    private final Integer code;

    private final String desc;
}
