package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 上下架状态
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum UpDownEnum implements BasicEnum<Integer, String> {

    UP(0, "上架"),
    DOWN(1, "下架"),
    ;

    private final Integer code;

    private final String desc;
    
    public static UpDownEnum getUpDownEnum(Integer code) {
        for (UpDownEnum upDownEnum : UpDownEnum.values()) {
            if (upDownEnum.getCode().equals(code)) {
                return upDownEnum;
            }
        }
        return null;
    }
}
