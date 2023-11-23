package com.xiliulou.electricity.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 批量盘点
 * @date 2023/11/20 21:33:56
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetInventoryDetailBatchInventoryRequest {
    
    /**
     * 盘点状态
     */
    private Integer status;
    
    /**
     * 盘点的电池sn码
     */
    private List<String> snList;
    
    /**
     * 操作者
     */
    private Long uid;
 
}
