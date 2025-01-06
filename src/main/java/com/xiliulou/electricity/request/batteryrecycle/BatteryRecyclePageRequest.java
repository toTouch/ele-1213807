package com.xiliulou.electricity.request.batteryrecycle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 区域查询
 * @date 2024/2/6 15:34:16
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryRecyclePageRequest {
    
    private Long size;
    
    private Long offset;
    
    private String sn;

    private List<String> snList;
    
    private String batchNo;
    
    private Integer status;
    
    private Integer electricityCabinetId;
    
    private Long franchiseeId;
    
    private Long startTime;
    
    private Long endTime;
    
    private Integer tenantId;
    
    private List<Long> franchiseeIdList;
}
