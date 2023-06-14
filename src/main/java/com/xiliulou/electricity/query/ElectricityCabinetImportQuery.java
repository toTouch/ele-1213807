package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-14-14:16
 */
@Data
public class ElectricityCabinetImportQuery {

    @NotBlank(message = "换电柜名称不能为空!")
    private String name;

    @NotBlank(message = "换电柜编码不能为空!")
    private String sn;

    @NotBlank(message = "换电柜地址不能为空!")
    private String address;

    @NotNull(message = "地址经度不能为空!")
    private Double longitude;

    @NotNull(message = "地址纬度不能为空!")
    private Double latitude;

    @NotBlank(message = "productKey不能为空!")
    private String productKey;

    @NotBlank(message = "deviceName不能为空!")
    private String deviceName;

    @NotNull(message = "可用状态不能为空!")
    private Integer usableStatus;

    @NotNull(message = "型号Id不能为空!")
    private Integer modelId;

    @NotNull(message = "满电标准不能为空!")
    private Double fullyCharged;

    @NotBlank(message = "联系电话不能为空!")
    private String servicePhone;

    @NotBlank(message = "营业时间不能为空!")
    private String businessTime;

    @NotNull(message = "门店id不能为空!")
    private Long storeId;
}
