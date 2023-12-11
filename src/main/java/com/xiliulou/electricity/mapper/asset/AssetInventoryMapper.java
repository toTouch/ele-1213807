package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.bo.asset.AssetInventoryBO;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetInventorySaveOrUpdateQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryUpdateDataQueryModel;
import com.xiliulou.electricity.vo.asset.AssetInventoryVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产盘点Mapper
 * @date 2023/11/20 13:09:28
 */

@Repository
public interface AssetInventoryMapper {
    
    Integer insertOne(AssetInventorySaveOrUpdateQueryModel assetInventorySaveOrUpdateQueryModel);
    
    List<AssetInventoryBO> selectListByFranchiseeId(AssetInventoryQueryModel assetInventoryQueryModel);
    
    Integer countTotal(AssetInventoryQueryModel assetInventoryQueryModel);
    
    AssetInventoryBO selectById(Long id);
    
    Integer selectInventoryStatusByFranchiseeId(@Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId, @Param("type") Integer type);
    
    Integer updateByOrderNo(AssetInventoryUpdateDataQueryModel assetInventoryUpdateDataQueryModel);
    
    AssetInventoryVO selectByOrderNo(AssetInventoryQueryModel assetInventoryQueryModel);
    
    Integer existInventoryByFranchiseeIdList(@Param("tenantId")Integer tenantId,@Param("franchiseeIdList")List<Long> franchiseeIdList, @Param("type") Integer type);
}
