package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName : TurnoverStatisticQueryModel
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TurnoverStatisticQueryModel {
    private Integer tenantId;
    
    private Long beginTime;
    
    private Long endTime;
    
    private List<Long> franchiseeIds;
    
    private List<Long> storeIds;
    
    private Long size;
    
    private Long offset;
}
