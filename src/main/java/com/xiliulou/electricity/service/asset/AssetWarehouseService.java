package com.xiliulou.electricity.service.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.asset.AssetWarehouseRequest;
import com.xiliulou.electricity.request.asset.AssetWarehouseSaveOrUpdateRequest;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.asset.AssetWarehouseNameVO;
import com.xiliulou.electricity.vo.asset.AssetWarehouseVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 库房业务
 * @date 2023/11/21 15:56:29
 */
public interface AssetWarehouseService {

    R save(AssetWarehouseSaveOrUpdateRequest assetWarehouseSaveOrUpdateRequest, Long uid);
    
    List<AssetWarehouseVO> listByFranchiseeId(AssetWarehouseRequest assetInventoryRequest);
    
    Integer countTotal(AssetWarehouseRequest assetInventoryRequest);
    
    List<AssetWarehouseNameVO> listWarehouseNames(AssetWarehouseRequest assetInventoryRequest);
    
    AssetWarehouseNameVO queryById(Long id);
    
    R deleteById(Long id);
    
    R updateById(AssetWarehouseSaveOrUpdateRequest assetWarehouseSaveOrUpdateRequest);
    
    Integer existsByName(String name);
}
