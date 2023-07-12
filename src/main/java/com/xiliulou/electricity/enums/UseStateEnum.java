package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 使用状态枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum UseStateEnum implements BasicEnum<Integer, String> {

    UN_USED(1, "未使用"),
    IN_USE(2, "使用中"),
    EXPIRED(3, "已失效"),
    RETURNED(4, "已退租"),
    ;

    private final Integer code;

    private final String desc;


    /**
     * 不可退的状态编码
     * @return
     */
    public static List<Integer> notRefundCodes() {
        return Arrays.asList(UseStateEnum.EXPIRED.getCode(), UseStateEnum.RETURNED.getCode());
    }

    /**
     * 可退的状态编码
     * @return
     */
    public static List<Integer> refundCodes() {
        return Arrays.asList(UseStateEnum.UN_USED.getCode(), UseStateEnum.IN_USE.getCode());
    }

}
