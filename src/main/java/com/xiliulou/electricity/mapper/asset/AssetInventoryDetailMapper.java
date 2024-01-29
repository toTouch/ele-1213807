package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.bo.asset.AssetInventoryDetailBO;
import com.xiliulou.electricity.query.asset.AssetInventoryDetailBatchInventoryQueryModel;
import com.xiliulou.electricity.query.asset.AssetInventoryDetailQueryModel;
import com.xiliulou.electricity.query.asset.AssetInventoryDetailSaveQueryModel;
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
    
    Integer batchInventoryBySnList(AssetInventoryDetailBatchInventoryQueryModel assetInventoryDetailBatchInventoryQueryModel);
    
    Integer batchInsert(@Param("inventoryDetailSaveQueryModelList") List<AssetInventoryDetailSaveQueryModel> inventoryDetailSaveQueryModelList);
    
    Integer countTotal(AssetInventoryDetailQueryModel assetInventoryDetailQueryModel);
    
    List<AssetInventoryDetailBO> selectListBySnListAndOrderNo(@Param("snList") List<String> snList, @Param("orderNo") String orderNo);
}
