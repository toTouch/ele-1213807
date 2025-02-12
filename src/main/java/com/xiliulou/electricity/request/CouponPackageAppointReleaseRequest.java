package com.xiliulou.electricity.request;


import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author : renhang
 * @description CouponPackageAppointReleaseRequest
 * @date : 2025-01-17 15:24
 **/
@Data
public class CouponPackageAppointReleaseRequest {

    @NotNull(message = "优惠券包不能为空")
    private Long packageId;

    @NotNull(message = "用户id不能为空")
    private List<Long> uid;
}
