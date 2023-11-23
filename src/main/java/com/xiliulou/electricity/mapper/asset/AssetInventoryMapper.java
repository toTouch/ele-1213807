package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.bo.asset.AssetInventoryBO;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetInventorySaveOrUpdateQueryModel;
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
    
    List<AssetInventoryBO> selectListByFranchiseeId(AssetInventoryQueryModel assetInventoryQueryModel);
    
    Integer countTotal(AssetInventoryQueryModel assetInventoryQueryModel);
    
    Integer updateById(AssetInventorySaveOrUpdateQueryModel assetInventorySaveOrUpdateQueryModel);
    
    AssetInventoryBO selectById(Long id);
}
