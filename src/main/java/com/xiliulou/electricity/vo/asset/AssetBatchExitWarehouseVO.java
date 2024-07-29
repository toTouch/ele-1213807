package com.xiliulou.electricity.vo.asset;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 批量退库vo
 * @date 2024/7/18 10:07:08
 */

@Builder
@Data
public class AssetBatchExitWarehouseVO {
    
    /**
     * 加盟商不一致的sn
     */
    private List<String> snListForNotSameFranchisee;
    
    /**
     * 状态为库存状态的sn
     */
    private List<String> snListForStockStatus;
    
}
