package com.xiliulou.electricity.enums.car;

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
public enum CarRentalTypeEnum implements BasicEnum<Integer, String> {

    RENTAL(1, "租车"),
    RETURN(2, "还车"),
    ;

    private final Integer code;

    private final String desc;
}
