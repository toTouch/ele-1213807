package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-03-13-9:56
 */
@Data
public class FreeCarBatteryDepositQuery {

    @NotNull(message = "加盟商id不能为空")
    private Long franchiseeId;

    @NotBlank(message = "手机号不能为空")
    private String phoneNumber;

    @NotBlank(message = "身份证不能为空")
    private String idCard;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

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
