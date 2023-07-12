package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 租赁单位枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum RentalUnitEnum implements BasicEnum<Integer, String> {

    NUMBER(0, "次数"),
    DAY(1, "天"),
    MINUTE(2, "分钟"),
    ;

    private final Integer code;

    private final String desc;

    /**
     * 获取余量单位编码集
     * @return
     */
    private static List<Integer> residueUnitCodes() {
        return Arrays.asList(RentalUnitEnum.DAY.getCode(), RentalUnitEnum.MINUTE.getCode(), RentalUnitEnum.NUMBER.getCode());
    }

    /**
     * 获取租期单位的编码集
     * @return
     */
    private static List<Integer> tenancyUnitCodes() {
        return Arrays.asList(RentalUnitEnum.DAY.getCode(), RentalUnitEnum.MINUTE.getCode());
    }
}
