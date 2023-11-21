package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.entity.asset.AssetWarehouse;
import org.springframework.stereotype.Repository;

/**
 * @author HeYafeng
 * @description 库房
 * @date 2023/11/21 16:34:29
 */

@Repository
public interface AssetWarehouseMapper {
    
    Integer insertOne(AssetWarehouse assetWarehouse);
}
