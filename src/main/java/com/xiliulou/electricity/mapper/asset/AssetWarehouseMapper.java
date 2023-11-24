package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.bo.asset.AssetWarehouseBO;
import com.xiliulou.electricity.bo.asset.AssetWarehouseNameBO;
import com.xiliulou.electricity.queryModel.asset.AssetWarehouseQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetWarehouseSaveOrUpdateQueryModel;
import com.xiliulou.electricity.vo.asset.AssetWarehouseNameVO;
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
    
    List<AssetWarehouseBO> selectListByFranchiseeId(AssetWarehouseQueryModel assetWarehouseQueryModel);
    
    Integer countTotal(AssetWarehouseQueryModel assetWarehouseQueryModel);
    
    List<AssetWarehouseNameBO> selectListWarehouseNames(AssetWarehouseQueryModel assetWarehouseQueryModel);
    
    Integer updateById(AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel);
    
    AssetWarehouseNameBO selectById(Long id);
    
    Integer existsByName(@Param("tenantId") Integer tenantId, @Param("name") String name);
}
