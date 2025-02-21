package com.xiliulou.electricity.vo.battery;

import lombok.Data;

/**
 * @author SJP
 * @date 2025-02-20 15:05
 **/
@Data
public class ElectricityBatteryLabelVO {
    /**
     * sn码
     */
    private String sn;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 领用人，管理员uid或者商户id
     */
    private Long receiverId;
    
    /**
     * 领用人名称
     */
    private String receiverName;
}
