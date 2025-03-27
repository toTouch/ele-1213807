package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: FreeServiceFeeStatusEnum
 * @Author: RenHang
 * @Date: 2025/03/27
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum FreeServiceFeeStatusEnum {

    STATUS_UNPAID(0, "未支付"),
    STATUS_SUCCESS(1, "成功"),
    STATUS_FAIL(2, "失败"),
    ;

    private Integer status;

    private String desc;
}
