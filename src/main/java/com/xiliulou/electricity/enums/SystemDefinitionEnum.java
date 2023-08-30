package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统定义枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum SystemDefinitionEnum implements BasicEnum<Integer, String> {

    WX_APPLET(1, "小程序"),
    BACKGROUND(2, "后台管理系统"),
    ;

    private final Integer code;

    private final String desc;
}
