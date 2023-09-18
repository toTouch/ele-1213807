package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会员操作类型枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum MemberOptTypeEnum implements BasicEnum<Integer, String> {

    NUMBER(0, "数字"),

    TIME(1, "时间戳"),

            ;

    private final Integer code;

    private final String desc;
}
