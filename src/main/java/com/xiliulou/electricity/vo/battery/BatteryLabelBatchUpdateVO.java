package com.xiliulou.electricity.vo.battery;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author SJP
 * @date 2025-02-20 18:48
 **/
@Data
@Builder
public class BatteryLabelBatchUpdateVO {
    
    private Integer successCount;
    
    private Integer failureCount;
    
    private List<Map<String, String>> failReasons;
}
