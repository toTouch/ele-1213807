package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ElectricityCabinetOutWarehouseRequest {
    /**
     * id
     */
    private Integer id;
    
    /**
     * 换电柜sn
     */
    @NotEmpty(message = "换电柜sn不能为空!", groups = {UpdateGroup.class})
    private String sn;
    
    /**
     * 厂家名称/型号
     */
    @NotNull(message = "厂家型号不能为空!", groups = {UpdateGroup.class})
    private Integer name;
    
    /**
     * 物联网productKey
     */
    @NotEmpty(message = "换电柜productKey不能为空!", groups = {UpdateGroup.class})
    private String franchiseeId;
    
    /**
     * 物联网deviceName
     */
    @NotEmpty(message = "门店不能为空!", groups = {UpdateGroup.class})
    private String storeId;
    
    /**
     * 换电柜地址
     */
    @NotEmpty(message = "换电柜地址不能为空!", groups = {UpdateGroup.class})
    private String address;
    
    /**
     * 地址经度
     */
    @NotNull(message = "地址经度不能为空!", groups = {UpdateGroup.class})
    private Double longitude;
    
    /**
     * 地址纬度
     */
    @NotNull(message = "地址纬度不能为空!", groups = {UpdateGroup.class})
    private Double latitude;
}
