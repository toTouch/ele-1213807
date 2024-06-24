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
public class ElectricityCabinetAddRequest {
    
    /**
     * 换电柜sn
     */
    @NotEmpty(message = "换电柜sn不能为空!")
    private String sn;
    
    /**
     * 厂家名称/型号
     */
    @NotNull(message = "厂家型号不能为空!", groups = {CreateGroup.class})
    private Integer modelId;
    
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
     * 电柜类型
     */
    @NotNull(message = "电柜类型exchangeType不能为空!", groups = {CreateGroup.class})
    private Integer exchangeType;
    
    /**
     * 库房Id
     */
    private Long warehouseId;
    
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
}
