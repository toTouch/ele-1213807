package com.xiliulou.electricity.query.asset;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author HeYafeng
 * @description 退库查询model
 * @date 2023/11/28 08:58:18
 */
@Builder
@Data
public class AssetEnableExitWarehouseQueryModel {
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private List<String> snList;
    
    private Set<Long> idSet;
    
    private Integer stockStatus;
}
