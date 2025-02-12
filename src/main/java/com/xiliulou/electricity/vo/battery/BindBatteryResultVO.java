package com.xiliulou.electricity.vo.battery;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 批量出库结果
 */
@Data
@Builder
public class BindBatteryResultVO {
    
    private Integer successCount;
    
    private Integer failureCount;
    
    private List<BindBatteryFailReasonVO> failReason;
}
