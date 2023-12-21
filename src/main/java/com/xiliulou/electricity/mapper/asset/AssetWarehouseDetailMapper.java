package com.xiliulou.electricity.mapper.asset;

import com.xiliulou.electricity.entity.asset.AssetWarehouseDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 库房资产记录详情
 * @date 2023/12/21 18:46:37
 */
@Mapper
public interface AssetWarehouseDetailMapper {
    
    Integer insertOne(AssetWarehouseDetail assetWarehouseDetail);
    
    Integer batchInsert(@Param("batchInsertList") List<AssetWarehouseDetail> batchInsertDetailList);
    
    List<AssetWarehouseDetail> selectListBySn(@Param("warehouseId") Long warehouseId, @Param("sn") String sn);
    
    List<AssetWarehouseDetail> selectListByRecordNo(String recordNo);
}
