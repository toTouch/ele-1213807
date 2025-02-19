package com.xiliulou.electricity.dto.battery;

import lombok.Data;

/**
 * @author SJP
 * @date 2025-02-18 19:46
 **/
@Data
public class BatteryLabelModifyDto {
    
    /**
     * 预修改电池标签
     */
    private Integer preLabel;
    
    /**
     * 操作人uid
     */
    private Long operatorUid;
}
