package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.asset.AssetWarehouse;
import com.xiliulou.electricity.mapper.asset.AssetWarehouseMapper;
import com.xiliulou.electricity.queryModel.asset.AssetWarehouseQueryModel;
import com.xiliulou.electricity.request.asset.AssetWarehouseRequest;
import com.xiliulou.electricity.request.asset.AssetWarehouseSaveRequest;
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
    public R save(AssetWarehouseSaveRequest assetWarehouseSaveRequest) {
        
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_WAREHOUSE_LOCK + assetWarehouseSaveRequest.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        AssetWarehouse assetWarehouse = AssetWarehouse.builder().name(assetWarehouseSaveRequest.getName()).status(assetWarehouseSaveRequest.getStatus())
                .managerName(assetWarehouseSaveRequest.getManagerName()).managerPhone(assetWarehouseSaveRequest.getManagerPhone()).address(assetWarehouseSaveRequest.getAddress())
                .delFlag(AssetWarehouse.DEL_NORMAL).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(TenantContextHolder.getTenantId().longValue()).build();
        
        return R.ok(assetWarehouseMapper.insertOne(assetWarehouse));
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
}
