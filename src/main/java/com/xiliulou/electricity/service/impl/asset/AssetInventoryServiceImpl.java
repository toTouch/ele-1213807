package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.asset.AssetInventory;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.mapper.asset.AssetInventoryMapper;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetInventorySaveOrUpdateQueryModel;
import com.xiliulou.electricity.request.asset.AssetInventoryRequest;
import com.xiliulou.electricity.request.asset.AssetInventorySaveOrUpdateRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.asset.AssetInventoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.asset.AssetInventoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产盘点服务
 * @date 2023/11/20 13:26:02
 */
@Service
public class AssetInventoryServiceImpl implements AssetInventoryService {
    
    @Autowired
    private AssetInventoryMapper assetInventoryMapper;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private ElectricityBatteryService electricityBatteryService;
    
    @Override
    public R save(AssetInventorySaveOrUpdateRequest assetInventorySaveOrUpdateRequest) {
        
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_INVENTORY_LOCK + assetInventorySaveOrUpdateRequest.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
    
    
        Integer tenantId = TenantContextHolder.getTenantId();
        Long franchiseeId = assetInventorySaveOrUpdateRequest.getFranchiseeId();
        // 默认资产类型是电池,获取查询电池数量
        ElectricityBatteryQuery electricityBatteryQuery = ElectricityBatteryQuery.builder().tenantId(tenantId).franchiseeId(franchiseeId).build();
        Object data = electricityBatteryService.queryCount(electricityBatteryQuery).getData();
        
        // 生成资产盘点订单
        AssetInventorySaveOrUpdateQueryModel assetInventorySaveOrUpdateQueryModel = AssetInventorySaveOrUpdateQueryModel.builder()
                .orderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.ASSET_INVENTORY, assetInventorySaveOrUpdateRequest.getUid()))
                .franchiseeId(franchiseeId)
                .type(AssetTypeEnum.ASSET_TYPE_BATTERY.getCode())
                .status(AssetInventory.ASSET_INVENTORY_STATUS_TAKING)
                .inventoriedTotal(NumberConstant.ZERO).pendingTotal((Integer) data)
                .finishTime(assetInventorySaveOrUpdateRequest.getFinishTime())
                .operator(assetInventorySaveOrUpdateRequest.getUid())
                .tenantId(tenantId.longValue())
                .delFlag(AssetInventory.DEL_NORMAL)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .build();
        
        return R.ok(assetInventoryMapper.insertOne(assetInventorySaveOrUpdateQueryModel));
    }
    
    @Override
    public Integer updateById(AssetInventorySaveOrUpdateRequest assetInventorySaveOrUpdateRequest) {
        
        AssetInventorySaveOrUpdateQueryModel assetInventorySaveOrUpdateQueryModel = AssetInventorySaveOrUpdateQueryModel.builder()
                .franchiseeId(assetInventorySaveOrUpdateRequest.getFranchiseeId())
                .finishTime(assetInventorySaveOrUpdateRequest.getFinishTime())
                .updateTime(System.currentTimeMillis())
                .build();
        return assetInventoryMapper.updateById(assetInventorySaveOrUpdateQueryModel);
    }
    
    @Slave
    @Override
    public List<AssetInventoryVO> listByFranchiseeId(AssetInventoryRequest assetInventoryRequest) {
        //模型转换
        AssetInventoryQueryModel assetInventoryQueryModel = new AssetInventoryQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetInventoryQueryModel);
        assetInventoryQueryModel.setTenantId(TenantContextHolder.getTenantId().longValue());
        
        return assetInventoryMapper.selectListByFranchiseeId(assetInventoryQueryModel);
    }
    
    @Override
    public Integer queryCount(AssetInventoryRequest assetInventoryRequest) {
        //模型转换
        AssetInventoryQueryModel assetInventoryQueryModel = new AssetInventoryQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetInventoryQueryModel);
        assetInventoryQueryModel.setTenantId(TenantContextHolder.getTenantId().longValue());
    
        return assetInventoryMapper.queryCount(assetInventoryQueryModel);
    }
    
    @Override
    public AssetInventoryVO queryById(Long id) {
        return assetInventoryMapper.selectById(id);
    }
    
    
}
