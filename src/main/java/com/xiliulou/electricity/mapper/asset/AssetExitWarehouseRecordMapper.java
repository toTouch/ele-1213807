package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseSaveQueryModel;
import org.springframework.stereotype.Repository;

/**
 * @author HeYafeng
 * @description 资产退库mapper
 * @date 2023/11/27 15:04:55
 */

@Repository
public interface AssetExitWarehouseRecordMapper {
    
    Integer insertOne(AssetExitWarehouseSaveQueryModel assetExitWarehouseSaveQueryModel);
}
