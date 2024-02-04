package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.bo.asset.AssetAllocateRecordBO;
import com.xiliulou.electricity.query.asset.AssetAllocateRecordPageQueryModel;
import com.xiliulou.electricity.query.asset.AssetAllocateRecordSaveQueryModel;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产调拨记录Mapper
 * @date 2023/11/20 11:30:12
 */

@Repository
public interface AssetAllocateRecordMapper {
    
    Integer insertOne(AssetAllocateRecordSaveQueryModel assetAllocateRecordSaveQueryModel);
    
    List<AssetAllocateRecordBO> selectListByPage(AssetAllocateRecordPageQueryModel queryModel);
    
    Integer countTotal(AssetAllocateRecordPageQueryModel queryModel);
}
