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
    @NotNull(message = "换电柜ID不能为空!", groups = {UpdateGroup.class})
    private Integer id;
    
    /**
     * 换电柜sn
     */
    @NotEmpty(message = "换电柜product不能为空!", groups = {UpdateGroup.class})
    private String productKey;
    
    /**
     * 换电柜sn
     */
    @NotEmpty(message = "换电柜deviceName不能为空!", groups = {UpdateGroup.class})
    private String deviceName;
    
    /**
     * 换电柜sn
     */
    @NotEmpty(message = "换电柜sn不能为空!", groups = {UpdateGroup.class})
    private String sn;
    
    /**
     * 电柜名称
     */
    @NotEmpty(message = "电柜名称不能为空!", groups = {UpdateGroup.class})
    private String name;
    
    /**
     * 加盟商id
     */
    @NotNull(message = "加盟商不能为空!", groups = {UpdateGroup.class})
    private Long franchiseeId;
    
    /**
     * 门店id
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
    
    /**
     * 电柜类型
     */
    @NotNull(message = "电柜类型exchangeType不能为空!", groups = {UpdateGroup.class})
    private Integer exchangeType;
    
    /**
     * 营业时间类型
     */
    private String businessTimeType;
    
    /**
     * 营业开始时间
     */
    private Long beginTime;
    /**
     * 营业结束时间
     */
    private Long endTime;
    
    /**
     * 满电标准
     */
    private Double fullyCharged;
    
    /**
     * 联系电话
     */
    private String servicePhone;
    
    /**
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;
}
