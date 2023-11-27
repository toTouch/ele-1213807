package com.xiliulou.electricity.service.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseSaveRequest;

/**
 * @author HeYafeng
 * @description 退库服务
 * @date 2023/11/27 09:34:54
 */
public interface AssetExitWarehouseRecordService {
    
    R save(AssetExitWarehouseSaveRequest assetExitWarehouseSaveRequest);
}
