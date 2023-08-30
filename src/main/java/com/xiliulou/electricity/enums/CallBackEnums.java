package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 回调枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum CallBackEnums implements BasicEnum<Integer, String> {
    // TODO 待定
    OK(0, "待定"),
    // 租车套餐购买订单
    CAR_RENAL_PACKAGE_ORDER(7, "CAR_RENAL_PACKAGE_ORDER"),
    ;

    /**
     * 订单类型
     */
    private final Integer code;

    /**
     * 订单附加信息，根据这个信息，找具体处理的回调方法
     */
    private final String desc;
}
