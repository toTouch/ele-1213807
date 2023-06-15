package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-14-16:41
 */
@Data
public class ElectricityCabinetTransferQuery {

    @NotBlank(message = "productKey不能为空!")
    private String productKey;

    @NotBlank(message = "deviceName不能为空!")
    private String deviceName;

    @NotNull(message = "门店id不能为空!")
    private Long storeId;

    private Double longitude;

    private Double latitude;

    private String address;
}
