package com.xiliulou.electricity.service.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.asset.AssetInventoryDetailBatchInventoryRequest;
import com.xiliulou.electricity.request.asset.AssetInventoryDetailRequest;

/**
 * @author HeYafeng
 * @description 资产盘点详情服务
 * @date 2023/11/20 17:22:22
 */
public interface AssetInventoryDetailService {
    
    R listByOrderNo(AssetInventoryDetailRequest assetInventoryRequest);
    
    R batchInventory(AssetInventoryDetailBatchInventoryRequest assetInventoryDetailBatchInventoryRequest);
}
