package com.xiliulou.electricity.dto;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-06-20-10:29
 */
@Data
public class VoltageCurrentChangeDTO {
    
    private Long electricityCabinetId;
    
    private Integer cellNo;
    
    //充电器电压
    private Double chargeV;
    
    //充电器电流
    private Double chargeA;
    
    //电池电压
    private Double batteryChargeV;
    
    //电池电流
    private Double batteryChargeA;
    
    private String sessionId;
    
    private String reportTime;
    
    private String createTime;
}
