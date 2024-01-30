package com.xiliulou.electricity.service.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.asset.AssetInventoryQueryModel;
import com.xiliulou.electricity.query.asset.AssetInventoryUpdateDataQueryModel;
import com.xiliulou.electricity.request.asset.AssetInventoryRequest;
import com.xiliulou.electricity.request.asset.AssetInventorySaveOrUpdateRequest;
import com.xiliulou.electricity.vo.asset.AssetInventoryVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产盘点业务
 * @date 2023/11/20 13:21:26
 */
public interface AssetInventoryService {
    
    R save(AssetInventorySaveOrUpdateRequest assetInventorySaveRequest, Long operator);
    
    List<AssetInventoryVO> listByPage(AssetInventoryRequest assetInventoryRequest);
    
    Integer countTotal(AssetInventoryRequest assetInventoryRequest);
    
    AssetInventoryVO queryById(Long id);
    
    /**
     * @param type 资产类型(1-电柜, 2-电池, 3-车辆)
     * @return 盘点状态 (0-进行中,1-完成)
     * @description 资产盘点状态查询
     * @date 2023/11/24 09:10:35
     * @author HeYafeng
     */
    Integer queryInventoryStatusByFranchiseeId(Long franchiseeId, Integer type);
    
    Integer updateByOrderNo(AssetInventoryUpdateDataQueryModel assetInventoryUpdateDataQueryModel);
    
    AssetInventoryVO queryByOrderNo(AssetInventoryQueryModel assetInventoryQueryModel);
    
    Integer existInventoryByFranchiseeIdList(List<Long> franchiseeIdList, Integer type);
}
