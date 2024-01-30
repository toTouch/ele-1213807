package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.bo.asset.AssetWarehouseRecordBO;
import com.xiliulou.electricity.entity.asset.AssetWarehouseRecord;
import com.xiliulou.electricity.query.asset.AssetWarehouseRecordQueryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 库房资产记录
 * @date 2023/12/20 10:34:19
 */
@Mapper
public interface AssetWarehouseRecordMapper {
    
    Integer batchInsert(@Param("batchInsertList") List<AssetWarehouseRecord> batchInsertList);
    
    Integer insertOne(AssetWarehouseRecord assetWarehouseRecord);
    
    List<AssetWarehouseRecordBO> selectListByRecordNoSet(AssetWarehouseRecordQueryModel queryModel);
    
    Integer countTotal(AssetWarehouseRecordQueryModel queryModel);
}
