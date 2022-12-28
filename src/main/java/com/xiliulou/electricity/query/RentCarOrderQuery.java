package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-27-17:44
 */
@Data
@Builder
public class RentCarOrderQuery {
    @NotBlank(message = "车辆编码不能为空!")
    private String sn;

    @NotNull(message = "门店id不能为空!")
    private Long storeId;

    @NotNull(message = "车辆型号不能为空!")
    private Long carModelId;

    @NotNull(message = "车辆租赁时间不能为空!")
    private Integer rentTime;

    @NotBlank(message = "车辆租赁方式不能为空!")
    private String rentType;

    @NotBlank(message = "用户名称不能为空!")
    private String username;

    @NotBlank(message = "用户手机号不能为空!")
    private String phone;

}
