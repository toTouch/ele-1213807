package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.bo.asset.AssetAllocateDetailBO;
import com.xiliulou.electricity.query.asset.AssetAllocateDetailSaveQueryModel;
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
    
    List<AssetAllocateDetailBO> selectListByPage(@Param("orderNo") String orderNo, @Param("tenantId") Integer tenantId);
}
