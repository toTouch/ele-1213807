package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *
 */
@Data
public class BatteryAddRequest {
    
    private Integer id;
    
    /**
     * sn码
     */
    @NotEmpty(message = "电池编码不能不能为空!", groups = {CreateGroup.class})
    private String sn;
    
    /**
     * 电池型号
     */
    private String batteryType;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 库房Id
     */
    private Long warehouseId;
    
    /**
     * 物联网卡号
     */
    private String iotCardNumber;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    private Integer isNeedSync;
}
