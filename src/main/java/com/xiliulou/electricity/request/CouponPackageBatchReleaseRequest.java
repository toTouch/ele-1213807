package com.xiliulou.electricity.request;


import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : renhang
 * @description CouponPackageBatchReleaseRequest
 * @date : 2025-01-17 14:19
 **/
@Data
public class CouponPackageBatchReleaseRequest {

    @NotNull(message = "优惠券包id不能为空")
    private Long packageId;

    @NotNull(message = "用户电话号不能为空")
    private String jsonPhones;
}
