package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-23-13:45
 */
@Data
public class FreeCarDepositHybridOrderQuery {
    @NotNull(message = "门店不能为空!")
    private Long storeId;

    @NotNull(message = "车辆型号不能为空!")
    private Long carModelId;

    @NotNull(message = "车辆租赁时间不能为空!")
    private Integer rentTime;

    @NotBlank(message = "车辆租赁方式不能为空!")
    private String rentType;
}
