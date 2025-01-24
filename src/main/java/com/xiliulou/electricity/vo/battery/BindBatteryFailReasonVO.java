package com.xiliulou.electricity.vo.battery;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 批量出库失败原因
 */
@Data
@AllArgsConstructor
public class BindBatteryFailReasonVO {
    
    private String failSn;
    
    private String reason;
}
