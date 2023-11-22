package com.xiliulou.electricity.service.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.asset.AssetInventoryRequest;
import com.xiliulou.electricity.request.asset.AssetInventorySaveOrUpdateRequest;
import com.xiliulou.electricity.vo.asset.AssetInventoryVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产盘点服务
 * @date 2023/11/20 13:21:26
 */
public interface AssetInventoryService {
    
    R save(AssetInventorySaveOrUpdateRequest assetInventorySaveRequest);
    
    Integer updateById(AssetInventorySaveOrUpdateRequest assetInventorySaveOrUpdateRequest);
    
    List<AssetInventoryVO> listByFranchiseeId(AssetInventoryRequest assetInventoryRequest);
    
    Integer queryCount(AssetInventoryRequest assetInventoryRequest);
    
    AssetInventoryVO queryById(Long id);
    
}
