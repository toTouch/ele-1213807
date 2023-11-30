package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseDetailQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseDetailSaveQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseSaveQueryModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产退库详情mapper
 * @date 2023/11/27 15:04:55
 */

@Repository
public interface AssetExitWarehouseDetailMapper {
    
    Integer batchInsert(@Param("detailSaveQueryModelList") List<AssetExitWarehouseDetailSaveQueryModel> detailSaveQueryModelList);
    
    List<String> selectListSnByOrderNo(AssetExitWarehouseDetailQueryModel assetExitWarehouseDetailQueryModel);
}
