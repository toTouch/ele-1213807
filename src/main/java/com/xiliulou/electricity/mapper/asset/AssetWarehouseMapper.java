package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.bo.asset.AssetWarehouseBO;
import com.xiliulou.electricity.bo.asset.AssetWarehouseNameBO;
import com.xiliulou.electricity.query.asset.AssetWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.AssetWarehouseSaveOrUpdateQueryModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HeYafeng
 * @description 库房
 * @date 2023/11/21 16:34:29
 */

@Repository
public interface AssetWarehouseMapper {
    
    Integer insertOne(AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel);
    
    List<AssetWarehouseBO> selectListByPage(AssetWarehouseQueryModel assetWarehouseQueryModel);
    
    Integer countTotal(AssetWarehouseQueryModel assetWarehouseQueryModel);
    
    List<AssetWarehouseNameBO> selectListWarehouseNames(AssetWarehouseQueryModel assetWarehouseQueryModel);
    
    Integer updateById(AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel);
    
    AssetWarehouseBO selectById(Long id);
    
    Integer existsByName(@Param("tenantId") Integer tenantId, @Param("name") String name);
    
    List<AssetWarehouseNameBO> selectListByIdList(@Param("tenantId") Integer tenantId,@Param("idList") List<Long> idList);
}
