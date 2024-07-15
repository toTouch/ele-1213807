package com.xiliulou.electricity.service.asset;

import com.xiliulou.electricity.bo.asset.AssetBatchExitWarehouseBO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产管理
 * @date 2024/7/2 18:32:02
 */
public interface AssetManageService {
    
    void batchExistWarehouseTx(List<AssetBatchExitWarehouseBO> dataList);
    
}
