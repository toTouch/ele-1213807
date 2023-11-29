package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.queryModel.asset.AssetAllocateRecordSaveQueryModel;
import org.springframework.stereotype.Repository;

/**
 * @author HeYafeng
 * @description 资产调拨记录Mapper
 * @date 2023/11/20 11:30:12
 */

@Repository
public interface AssetAllocateRecordMapper {
    
    Integer insertOne(AssetAllocateRecordSaveQueryModel assetAllocateRecordSaveQueryModel);
}
