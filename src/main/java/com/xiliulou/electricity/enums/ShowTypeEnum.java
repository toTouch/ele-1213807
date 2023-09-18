package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-07-15:18
 */
@Getter
@AllArgsConstructor
public enum ShowTypeEnum implements BasicEnum<Integer, String> {
    /**
     * 0:显示,1:隐藏
     */
    DISPLAY(0, "显示"),
    HIDDEN(1, "隐藏");

    private final Integer code;

    private final String desc;
}
