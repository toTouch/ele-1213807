package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.queryModel.asset.AssetInventoryDetailQueryModel;
import com.xiliulou.electricity.vo.asset.AssetInventoryDetailVO;
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
    
    List<AssetInventoryDetailVO> selectListByOrderNo(AssetInventoryDetailQueryModel assetInventoryQueryDetailModel);
    
    Integer batchInventoryBySnList(@Param("status") Integer status, @Param("snList") List<String> snList, @Param("tenantId") Long tenantId);
}
