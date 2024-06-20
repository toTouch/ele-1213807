package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.asset.AssetWarehouseDetail;
import com.xiliulou.electricity.mapper.asset.AssetWarehouseDetailMapper;
import com.xiliulou.electricity.service.asset.AssetWarehouseDetailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HeYafeng
 * @description 库房资产记录详情业务
 * @date 2023/12/21 18:43:14
 */
@Service
public class AssetWarehouseDetailServiceImpl implements AssetWarehouseDetailService {
    
    @Resource
    private AssetWarehouseDetailMapper assetWarehouseDetailMapper;
    
    @Override
    public Integer insertOne(AssetWarehouseDetail assetWarehouseDetail) {
        return assetWarehouseDetailMapper.insertOne(assetWarehouseDetail);
    }
    
    @Override
    public Integer batchInsert(List<AssetWarehouseDetail> batchInsertDetailList) {
        return assetWarehouseDetailMapper.batchInsert(batchInsertDetailList);
    }
    
    @Slave
    @Override
    public List<AssetWarehouseDetail> listBySn(Long warehouseId, String sn) {
        return assetWarehouseDetailMapper.selectListBySn(warehouseId, sn);
    }
    
    @Slave
    @Override
    public List<AssetWarehouseDetail> listByRecordNo(String recordNo) {
        return assetWarehouseDetailMapper.selectListByRecordNo(recordNo);
    }
    
    @Slave
    @Override
    public List<AssetWarehouseDetail> listByRecordNoList(List<String> recordNoList) {
        return assetWarehouseDetailMapper.selectListByRecordNoList(recordNoList);
    }
}
