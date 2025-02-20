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
     * 领用管理员名称
     */
    private String administratorName;
    
    /**
     * 领用商户名称
     */
    private Long merchantName;
}
