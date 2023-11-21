package com.xiliulou.electricity.service.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.asset.AssetWarehouseRequest;
import com.xiliulou.electricity.request.asset.AssetWarehouseSaveRequest;
import com.xiliulou.electricity.vo.asset.AssetWarehouseNameVO;
import com.xiliulou.electricity.vo.asset.AssetWarehouseVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 库房服务
 * @date 2023/11/21 15:56:29
 */
public interface AssetWarehouseService {

    R save(AssetWarehouseSaveRequest assetWarehouseSaveRequest);
    
    List<AssetWarehouseVO> listByFranchiseeId(AssetWarehouseRequest assetInventoryRequest);
    
    Integer queryCount(AssetWarehouseRequest assetInventoryRequest);
    
    List<AssetWarehouseNameVO> listWarehouseNameByFranchiseeId();
}
