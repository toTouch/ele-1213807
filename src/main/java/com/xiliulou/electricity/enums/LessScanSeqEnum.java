package com.xiliulou.electricity.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
@SuppressWarnings("all")
public enum LessScanSeqEnum {


    RETURN_BATTERY_TWO_SCAN_CLOSE_CELL(61, "归还仓门关门", "号仓门仓门关闭"),

    RETURN_BATTERY_TWO_SCAN_CHECK(66, "归还电池检测", "归还电池成功"),

    ;

    private final Integer seq;

    private final String desc;

    private final String result;
}
