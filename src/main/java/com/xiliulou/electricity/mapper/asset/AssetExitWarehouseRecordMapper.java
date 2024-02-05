package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.bo.asset.AssetExitWarehouseBO;
import com.xiliulou.electricity.query.asset.AssetExitWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.AssetExitWarehouseSaveQueryModel;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产退库mapper
 * @date 2023/11/27 15:04:55
 */

@Repository
public interface AssetExitWarehouseRecordMapper {
    
    Integer insertOne(AssetExitWarehouseSaveQueryModel assetExitWarehouseSaveQueryModel);
    
    List<AssetExitWarehouseBO> selectListByFranchiseeId(AssetExitWarehouseQueryModel assetExitWarehouseQueryModel);
    
    Integer countTotal(AssetExitWarehouseQueryModel assetExitWarehouseQueryModel);
}
