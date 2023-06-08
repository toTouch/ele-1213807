package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-05-24-18:54
 */
@Data
public class ElectricityCabinetAddressQuery {

    /**
     * 换电柜Id
     */
    @NotNull(message = "换电柜Id不能为空!")
    private Integer id;
    /**
     * 换电柜地址
     */
    @NotEmpty(message = "换电柜地址不能为空!")
    private String address;
    /**
     * 地址经度
     */
    @NotNull(message = "地址经度不能为空!")
    private Double longitude;
    /**
     * 地址纬度
     */
    @NotNull(message = "地址纬度不能为空!")
    private Double latitude;


}
