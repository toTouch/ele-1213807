package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会员套餐期限状态枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum MemberTermStatusEnum implements BasicEnum<Integer, String> {

    PENDING_EFFECTIVE(0, "待生效"),
    NORMAL(1, "正常"),
    APPLY_FREEZE(2, "申请冻结"),
    FREEZE(3, "冻结"),
    APPLY_REFUND_DEPOSIT(4, "申请退押"),
    APPLY_RENT_REFUND(5, "申请退租"),
    ;

    private final Integer code;

    private final String desc;

}
