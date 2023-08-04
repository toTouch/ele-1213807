package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 租赁套餐冻结订单状态枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum RentalPackageOrderFreezeStatusEnum implements BasicEnum<Integer, String> {

    PENDING_APPROVAL(1, "待审核"),
    AUDIT_PASS(2, "审核通过"),
    AUDIT_REJECT(3, "审核拒绝"),
    EARLY_ENABLE(4, "提前启用"),
    AUTO_ENABLE(5, "自动启用"),
    REVOKE(6, "撤回申请"),
    LOSE_EFFICACY(7, "已失效"),
    ;

    private final Integer code;

    private final String desc;

}
