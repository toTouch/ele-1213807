package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.asset.AssetWarehouse;
import com.xiliulou.electricity.mapper.asset.AssetWarehouseMapper;
import com.xiliulou.electricity.queryModel.asset.AssetWarehouseQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetWarehouseSaveOrUpdateQueryModel;
import com.xiliulou.electricity.queue.asset.AssetWarehouseRequest;
import com.xiliulou.electricity.queue.asset.AssetWarehouseSaveOrUpdateRequest;
import com.xiliulou.electricity.service.asset.AssetWarehouseService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.asset.AssetWarehouseNameVO;
import com.xiliulou.electricity.vo.asset.AssetWarehouseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author HeYafeng
 * @description 库房服务
 * @date 2023/11/21 16:04:01
 */

@Service
public class AssetWarehouseServiceImpl implements AssetWarehouseService {
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private AssetWarehouseMapper assetWarehouseMapper;
    
    @Override
    public R save(AssetWarehouseSaveOrUpdateRequest assetWarehouseSaveOrUpdateRequest) {
        
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_WAREHOUSE_LOCK + assetWarehouseSaveOrUpdateRequest.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel = AssetWarehouseSaveOrUpdateQueryModel.builder().name(assetWarehouseSaveOrUpdateRequest.getName())
                .status(assetWarehouseSaveOrUpdateRequest.getStatus()).managerName(assetWarehouseSaveOrUpdateRequest.getManagerName()).managerPhone(
                        assetWarehouseSaveOrUpdateRequest.getManagerPhone())
                .address(assetWarehouseSaveOrUpdateRequest.getAddress()).delFlag(AssetWarehouse.DEL_NORMAL).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(TenantContextHolder.getTenantId().longValue()).build();
        
        return R.ok(assetWarehouseMapper.insertOne(warehouseSaveOrUpdateQueryModel));
    }
    
    @Override
    public List<AssetWarehouseVO> listByFranchiseeId(AssetWarehouseRequest assetInventoryRequest) {
        
        AssetWarehouseQueryModel assetWarehouseQueryModel = new AssetWarehouseQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetWarehouseQueryModel);
        assetWarehouseQueryModel.setTenantId(TenantContextHolder.getTenantId().longValue());
        
        return assetWarehouseMapper.selectListByFranchiseeId(assetWarehouseQueryModel);
    }
    
    public Integer queryCount(AssetWarehouseRequest assetInventoryRequest) {
        AssetWarehouseQueryModel assetWarehouseQueryModel = new AssetWarehouseQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetWarehouseQueryModel);
        assetWarehouseQueryModel.setTenantId(TenantContextHolder.getTenantId().longValue());
        
        return assetWarehouseMapper.queryCount(assetWarehouseQueryModel);
    }
    
    @Override
    public List<AssetWarehouseNameVO> listWarehouseNameByFranchiseeId() {
        
        return assetWarehouseMapper.selectListWarehouseNameByFranchiseeId(TenantContextHolder.getTenantId().longValue());
    }
    
    @Override
    public R deleteById(Long id) {
        //TODO 该库房有电柜/电池/车辆正在使用，请先解绑后操作
        
        AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel = AssetWarehouseSaveOrUpdateQueryModel.builder().id(id).delFlag(AssetWarehouse.DEL_DEL)
                .updateTime(System.currentTimeMillis()).tenantId(TenantContextHolder.getTenantId().longValue()).build();
        
        return R.ok(assetWarehouseMapper.updateById(warehouseSaveOrUpdateQueryModel));
    }
    
    @Override
    public Integer updateById(AssetWarehouseSaveOrUpdateRequest assetWarehouseSaveOrUpdateRequest) {
        AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel = new AssetWarehouseSaveOrUpdateQueryModel();
        BeanUtils.copyProperties(assetWarehouseSaveOrUpdateRequest, warehouseSaveOrUpdateQueryModel);
        
        return assetWarehouseMapper.updateById(warehouseSaveOrUpdateQueryModel);
    }
}
