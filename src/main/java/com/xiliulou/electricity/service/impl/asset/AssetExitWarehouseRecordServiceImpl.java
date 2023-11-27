package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.asset.AssetWarehouse;
import com.xiliulou.electricity.queryModel.asset.AssetWarehouseSaveOrUpdateQueryModel;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseSaveRequest;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author HeYafeng
 * @description 退库服务
 * @date 2023/11/27 09:36:23
 */
@Service
public class AssetExitWarehouseRecordServiceImpl implements AssetExitWarehouseRecordService {
    @Autowired
    private RedisService redisService;
    
    @Override
    public R save(AssetExitWarehouseSaveRequest assetExitWarehouseSaveRequest) {
    
        /*boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_WAREHOUSE_LOCK + CACHE_ASSET_EXIT_WAREHOUSE_RECORD_LOCK.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        Integer exists = existsByName(assetWarehouseSaveOrUpdateRequest.getName());
        if (Objects.nonNull(exists)) {
            return R.fail("300803", "库房名称已存在");
        }
    
        AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel = AssetWarehouseSaveOrUpdateQueryModel.builder().name(assetWarehouseSaveOrUpdateRequest.getName())
                .status(assetWarehouseSaveOrUpdateRequest.getStatus()).managerName(assetWarehouseSaveOrUpdateRequest.getManagerName())
                .managerPhone(assetWarehouseSaveOrUpdateRequest.getManagerPhone()).address(assetWarehouseSaveOrUpdateRequest.getAddress()).delFlag(AssetWarehouse.DEL_NORMAL)
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).tenantId(TenantContextHolder.getTenantId()).build();
    
        return R.ok(assetWarehouseMapper.insertOne(warehouseSaveOrUpdateQueryModel));
        
        
        
        */
        return null;
    }
}
