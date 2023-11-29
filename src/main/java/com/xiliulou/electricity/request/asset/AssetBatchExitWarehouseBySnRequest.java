package com.xiliulou.electricity.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 根据sn修改库存状态request
 * @date 2023/11/27 15:44:18
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AssetBatchExitWarehouseBySnRequest {
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private Long warehouseId;
    
    private List<String> snList;

}
