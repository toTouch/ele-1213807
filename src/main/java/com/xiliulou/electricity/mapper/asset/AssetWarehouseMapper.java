package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.queryModel.asset.AssetWarehouseQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetWarehouseSaveOrUpdateQueryModel;
import com.xiliulou.electricity.vo.asset.AssetWarehouseNameVO;
import com.xiliulou.electricity.vo.asset.AssetWarehouseVO;
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
    
    List<AssetWarehouseVO> selectListByFranchiseeId(AssetWarehouseQueryModel assetWarehouseQueryModel);
    
    Integer queryCount(AssetWarehouseQueryModel assetWarehouseQueryModel);
    
    List<AssetWarehouseNameVO> selectListWarehouseNameByFranchiseeId(Long tenantId);
    
    Integer updateById(AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel);
    
    AssetWarehouseNameVO selectById(Long id);
}
