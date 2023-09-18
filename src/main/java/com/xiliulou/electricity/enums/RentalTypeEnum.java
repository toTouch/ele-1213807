package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 车辆租赁类型枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum RentalTypeEnum implements BasicEnum<Integer, String> {

    RENTAL(1, "租借"),
    RETURN(2, "归还"),
    ;

    private final Integer code;

    private final String desc;
}
