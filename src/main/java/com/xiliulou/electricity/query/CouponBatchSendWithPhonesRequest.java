package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2024/2/18 18:00
 */
@Data
public class CouponBatchSendWithPhonesRequest {
    @NotNull(message = "优惠券id不能为空")
    private Integer couponId;

    @NotNull(message = "用户电话号不能为空")
    private String jsonPhones;
}
