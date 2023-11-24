package com.xiliulou.electricity.service.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryUpdateDataQueryModel;
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
    
    R updateById(AssetInventorySaveOrUpdateRequest assetInventorySaveOrUpdateRequest);
    
    List<AssetInventoryVO> listByFranchiseeId(AssetInventoryRequest assetInventoryRequest);
    
    Integer countTotal(AssetInventoryRequest assetInventoryRequest);
    
    AssetInventoryVO queryById(Long id);
    
    /**
     * @description 资产盘点状态查询
     * @param type 资产类型(1-电柜, 2-电池, 3-车辆)
     * @date 2023/11/24 09:10:35
     * @author HeYafeng
     * @return 盘点状态 (0-进行中,1-完成)
     */
    Integer queryInventoryStatusByFranchiseeId(Long franchiseeId, Integer type);
    
    Integer updateByOrderNo(AssetInventoryUpdateDataQueryModel assetInventoryUpdateDataQueryModel);
}
