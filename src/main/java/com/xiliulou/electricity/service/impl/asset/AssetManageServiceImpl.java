package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.electricity.mapper.asset.AssetExitWarehouseRecordMapper;
import com.xiliulou.electricity.query.asset.AssetExitWarehouseSaveQueryModel;
import com.xiliulou.electricity.service.asset.AssetManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HeYafeng
 * @description 资产管理
 * @date 2024/7/2 18:32:29
 */
@Slf4j
@Service
public class AssetManageServiceImpl implements AssetManageService {
    
    @Resource
    private AssetExitWarehouseRecordMapper assetExitWarehouseRecordMapper;
    
    @Override
    public Integer insertExitWarehouse(AssetExitWarehouseSaveQueryModel recordSaveQueryModel) {
        return assetExitWarehouseRecordMapper.insertOne(recordSaveQueryModel);
    }
}
