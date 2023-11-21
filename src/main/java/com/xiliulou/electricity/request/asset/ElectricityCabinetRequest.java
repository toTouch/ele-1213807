package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *
 */
@Data
public class ElectricityCabinetRequest {
    /**
     * 换电柜Id
     */
    @NotNull(message = "换电柜Id不能为空!", groups = {UpdateGroup.class})
    private Integer id;
    
    /**
     * 换电柜sn
     */
    @NotEmpty(message = "换电柜sn不能为空!", groups = {CreateGroup.class})
    private String sn;
    
    /**
     * 换电柜sn
     */
    @NotEmpty(message = "厂家型号不能为空!", groups = {CreateGroup.class})
    private String manufacturerModel;
    
    /**
     * 物联网productKey
     */
    @NotEmpty(message = "换电柜productKey不能为空!", groups = {CreateGroup.class})
    private String productKey;
    
    /**
     * 物联网deviceName
     */
    @NotEmpty(message = "换电柜deviceName不能为空!", groups = {CreateGroup.class})
    private String deviceName;
    
    /**
     * 库房Id
     */
    private String warehouseId;
    
    /**
     * 换电柜名称
     */
    @NotEmpty(message = "换电柜名称不能为空!", groups = {UpdateGroup.class})
    private String name;
    /**
     * 换电柜地址
     */
    private String address;
    
    /**
     * 加盟商id
     */
    private String franchiseeId;
    
    /**
     * 门店id
     */
    private Long storeId;
    
    /**
     * 地址经度
     */
    private Double longitude;
    
    /**
     * 地址纬度
     */
    private Double latitude;
    
    
    /**
     * 物联网deviceSecret
     */
    private String deviceSecret;
    
    /**
     * 可用状态(0--启用，1--禁用)
     */
    @NotNull(message = "可用状态不能为空!", groups = {CreateGroup.class})
    private Integer usableStatus;
    /**
     * 物联网连接状态（0--连网，1--断网）
     */
    private Integer onlineStatus;
    /**
     * 型号Id
     */
    @NotNull(message = "型号Id不能为空!", groups = {CreateGroup.class})
    private Integer modelId;
    /**
     * 版本
     */
    private String version;
    /**
     * 满电标准
     */
    private Double fullyCharged;
    
    /**
     * 联系电话
     */
    @NotEmpty(message = "联系电话不能为空!", groups = {CreateGroup.class})
    private String servicePhone;
    
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
}
