package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.bo.asset.AssetInventoryDetailBO;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryDetailQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryDetailSaveQueryModel;
import com.xiliulou.electricity.request.asset.AssetInventoryDetailBatchInventoryRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产盘点详情信息Mapper
 * @date 2023/11/20 11:30:12
 */

@Repository
public interface AssetInventoryDetailMapper {
    
    List<AssetInventoryDetailBO> selectListByOrderNo(AssetInventoryDetailQueryModel assetInventoryQueryDetailModel);
    
    Integer batchInventoryBySnList(AssetInventoryDetailBatchInventoryRequest inventoryRequest);
    
    Integer batchInsert(@Param("inventoryDetailSaveQueryModelList") List<AssetInventoryDetailSaveQueryModel> inventoryDetailSaveQueryModelList);
    
    Integer countTotal(AssetInventoryDetailQueryModel assetInventoryDetailQueryModel);
}
