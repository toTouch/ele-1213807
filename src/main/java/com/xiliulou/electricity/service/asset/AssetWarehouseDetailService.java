package com.xiliulou.electricity.service.asset;

import com.xiliulou.electricity.entity.asset.AssetWarehouseDetail;

import java.util.List;

/**
 * @author HeYafeng
 * @description 库房资产记录详情业务
 * @date 2023/12/21 18:42:46
 */
public interface AssetWarehouseDetailService {
    
    Integer insertOne(AssetWarehouseDetail assetWarehouseDetail);
    
    Integer batchInsert(List<AssetWarehouseDetail> batchInsertDetailList);
    
    List<AssetWarehouseDetail> listBySn(Long warehouseId, String sn);
    
    List<AssetWarehouseDetail> listByRecordNo(String recordNo);
}
