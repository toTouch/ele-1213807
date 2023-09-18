package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 退款单状态枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum RefundStateEnum implements BasicEnum<Integer, String> {

    PENDING_APPROVAL(1, "待审核"),
    AUDIT_PASS(2, "审核通过"),
    AUDIT_REJECT(3, "审核拒绝"),
    REFUNDING(4, "退款中"),
    SUCCESS(5, "退款成功"),
    FAILED(6, "退款失败"),
    ;

    private final Integer code;

    private final String desc;

    /**
     * 允许退款的状态
     * @return
     */
    public static List<Integer> getRefundStateList() {
        List<Integer> list = new ArrayList<>();
        list.add(RefundStateEnum.AUDIT_REJECT.getCode());
        list.add(RefundStateEnum.FAILED.getCode());
        return list;
    }
}
