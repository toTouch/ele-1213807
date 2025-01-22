package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 柜机电池变化记录
 * @date 2024/11/26 20:03:25
 */
@Data
public class BatteryChangeInfoVO {
    
    private String electricityCabinetId;
    
    private Integer cellNo;
    
    private String sessionId;
    
    private String preBatteryName;
    
    private String changeBatteryName;
    
    private Long reportTime;
    
    private Long createTime;
    
    private String orderId;
    
    private Integer operateType;
    
    private Long uid;
    
    private String userName;
    
}
