package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-22-17:21
 */
@Data
public class freeBatteryDepositHybridOrderQuery {
    private String productKey;

    private String deviceName;

    /**
     * 套餐id
     */
    @NotNull(message = "套餐Id不能为空!")
    private Integer memberCardId;

    /**
     * 保险id
     */
    private Integer insuranceId;

    /**
     * 优惠券id
     */
    private Integer userCouponId;

}
