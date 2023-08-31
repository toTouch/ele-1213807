package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum PayStateEnum implements BasicEnum<Integer, String> {

    UNPAID(1, "未支付"),
    SUCCESS(2, "支付成功"),
    FAILED(3, "支付失败"),
    CANCEL(4, "取消支付"),
    CLEAN_UP(5, "已清除"),
    ;

    private final Integer code;

    private final String desc;
}
