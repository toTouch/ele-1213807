package com.xiliulou.electricity.queue.asset;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ElectricityCabinetBatchOutWarehouseRequest {
    /**
     * id
     */
    @NotEmpty(message = "门店不能为空!", groups = {UpdateGroup.class})
    private List<Integer> idList;
    
    /**
     * 物联网productKey
     */
    @NotNull(message = "换电柜productKey不能为空!", groups = {UpdateGroup.class})
    private Long franchiseeId;
    
    /**
     * 物联网deviceName
     */
    @NotNull(message = "门店不能为空!", groups = {UpdateGroup.class})
    private Long storeId;
    
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
