package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.entity.asset.AssetInventory;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetInventorySaveOrUpdateQueryModel;
import com.xiliulou.electricity.vo.asset.AssetInventoryVO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产盘点Mapper
 * @date 2023/11/20 13:09:28
 */

@Repository
public interface AssetInventoryMapper {
    
    Integer insertOne(AssetInventorySaveOrUpdateQueryModel assetInventorySaveOrUpdateQueryModel);
    
    List<AssetInventoryVO> selectListByFranchiseeId(AssetInventoryQueryModel assetInventoryQueryModel);
    
    Integer queryCount(AssetInventoryQueryModel assetInventoryQueryModel);
    
    Integer updateById(AssetInventorySaveOrUpdateQueryModel assetInventorySaveOrUpdateQueryModel);
    
    AssetInventoryVO selectById(Long id);
}
