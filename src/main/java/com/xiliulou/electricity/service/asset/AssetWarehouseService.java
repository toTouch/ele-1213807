package com.xiliulou.electricity.service.asset;

import com.xiliulou.electricity.request.asset.AssetWarehouseSaveRequest;

/**
 * @author HeYafeng
 * @description 库房服务
 * @date 2023/11/21 15:56:29
 */
public interface AssetWarehouseService {

    Integer save(AssetWarehouseSaveRequest assetWarehouseSaveRequest);

}
