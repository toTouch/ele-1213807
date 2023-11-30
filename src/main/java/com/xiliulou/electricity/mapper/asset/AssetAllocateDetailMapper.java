package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.bo.asset.AssetInventoryDetailBO;
import com.xiliulou.electricity.queryModel.asset.AssetAllocateDetailSaveQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryDetailBatchInventoryQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryDetailQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryDetailSaveQueryModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产调拨详情Mapper
 * @date 2023/11/20 11:30:12
 */

@Repository
public interface AssetAllocateDetailMapper {
    
    Integer batchInsert(@Param("detailSaveQueryModelList") List<AssetAllocateDetailSaveQueryModel> detailSaveQueryModelList);
}
