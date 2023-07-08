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
    APPLY_FREEZE(1, "申请冻结"),
    FREEZE(1, "冻结"),
    APPLY_REFUND_DEPOSIT(1, "申请退押"),
    ;

    private final Integer code;

    private final String desc;

}
