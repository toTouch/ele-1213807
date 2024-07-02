package com.xiliulou.electricity.service.asset;

import com.xiliulou.electricity.query.asset.AssetExitWarehouseSaveQueryModel;

/**
 * @author HeYafeng
 * @description 资产管理
 * @date 2024/7/2 18:32:02
 */
public interface AssetManageService {
    
    Integer insertExitWarehouse(AssetExitWarehouseSaveQueryModel recordSaveQueryModel);
}
