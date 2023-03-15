package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-03-13-15:57
 */
@Data
public class FreeCarBatteryDepositOrderQuery {
    @NotNull(message = "加盟商id不能为空")
    private Long franchiseeId;

    @NotNull(message = "门店不能为空!")
    private Long storeId;

    @NotNull(message = "车辆型号不能为空!")
    private Long carModelId;

    @NotNull(message = "车辆租赁时间不能为空!")
    private Integer rentTime;

    @NotBlank(message = "车辆租赁方式不能为空!")
    private String rentType;

    @NotNull(message = "套餐不能为空!")
    private Integer memberCardId;

    @NotNull(message = "电池型号不能为空!")
    private Integer model;

    private Integer insuranceId;

    private Integer userCouponId;

    private String productKey;

    private String deviceName;
}
