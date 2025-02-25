package com.xiliulou.electricity.enums.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum MerchantWithdrawApplicationStateEnum {
    ACCEPTED(0, "ACCEPTED"),
    PROCESSING(1, "PROCESSING"),
    WAIT_USER_CONFIRM(2, "WAIT_USER_CONFIRM"),
    TRANSFERING(3, "TRANSFERING"),
    SUCCESS(4, "SUCCESS"),
    FAIL(5, "FAIL"),
    CANCELING(6, "CANCELING"),
    CANCELLED(7, "CANCELLED"),
    ;
    private final Integer code;

    private final String desc;

    public static MerchantWithdrawApplicationStateEnum getStateByDesc(String desc) {
        Optional<MerchantWithdrawApplicationStateEnum> first = Arrays.stream(values()).filter(item -> Objects.equals(item.getDesc(), desc)).findFirst();
        return first.orElse(null);
    }
}
