package com.xiliulou.electricity.enums.car;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 车辆租赁订单状态枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum CarRentalStateEnum implements BasicEnum<Integer, String> {

    AUDIT_ING(1, "审核中"),
    SUCCESS(2, "成功"),
    AUDIT_REJECT(3, "审核拒绝"),
    ;

    private final Integer code;

    private final String desc;
}