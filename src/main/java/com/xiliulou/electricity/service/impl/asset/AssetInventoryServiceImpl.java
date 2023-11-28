package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.asset.AssetInventoryBO;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.mapper.asset.AssetInventoryMapper;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetInventorySaveOrUpdateQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryUpdateDataQueryModel;
import com.xiliulou.electricity.queryModel.electricityBattery.ElectricityBatteryListSnByFranchiseeQueryModel;
import com.xiliulou.electricity.request.asset.AssetInventoryRequest;
import com.xiliulou.electricity.request.asset.AssetInventorySaveOrUpdateRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.asset.AssetInventoryDetailService;
import com.xiliulou.electricity.service.asset.AssetInventoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.asset.AssetInventoryVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 资产盘点业务
 * @date 2023/11/20 13:26:02
 */
@Service
public class AssetInventoryServiceImpl implements AssetInventoryService {
    
    protected XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("ASSET_INVENTORY_BATTERY_HANDLE_THREAD_POOL", 3,
            "asset_inventory_battery_handle_thread");
    
    @Autowired
    private AssetInventoryMapper assetInventoryMapper;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    private AssetInventoryDetailService assetInventoryDetailService;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Override
    public R save(AssetInventorySaveOrUpdateRequest assetInventorySaveOrUpdateRequest, Long operator) {
    
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_INVENTORY_LOCK + operator, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
    
        try {
            Integer tenantId = TenantContextHolder.getTenantId();
            Long franchiseeId = assetInventorySaveOrUpdateRequest.getFranchiseeId();
            String orderNo = OrderIdUtil.generateBusinessOrderId(BusinessType.ASSET_INVENTORY, operator);
            // 默认资产类型是电池,获取查询电池数量
            ElectricityBatteryQuery electricityBatteryQuery = ElectricityBatteryQuery.builder().tenantId(tenantId).franchiseeId(franchiseeId).build();
            Object data = electricityBatteryService.queryCount(electricityBatteryQuery).getData();
        
            // 生成资产盘点订单
            AssetInventorySaveOrUpdateQueryModel assetInventorySaveOrUpdateQueryModel = AssetInventorySaveOrUpdateQueryModel.builder().orderNo(orderNo).franchiseeId(franchiseeId)
                    .type(AssetTypeEnum.ASSET_TYPE_BATTERY.getCode()).status(AssetConstant.ASSET_INVENTORY_STATUS_TAKING).inventoriedTotal(NumberConstant.ZERO)
                    .pendingTotal((Integer) data).finishTime(assetInventorySaveOrUpdateRequest.getFinishTime()).operator(operator).tenantId(tenantId)
                    .delFlag(AssetConstant.DEL_NORMAL).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        
            //异步执行将电池数据导入到资产详情表
            if ((Integer) data > NumberConstant.ZERO) {
                ElectricityBatteryListSnByFranchiseeQueryModel queryModel = ElectricityBatteryListSnByFranchiseeQueryModel.builder().tenantId(tenantId).franchiseeId(franchiseeId)
                        .build();
                executorService.execute(() -> {
                    assetInventoryDetailService.asyncBatteryProcess(queryModel, orderNo, operator);
                });
            }
        
            return R.ok(assetInventoryMapper.insertOne(assetInventorySaveOrUpdateQueryModel));
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_INVENTORY_LOCK + operator);
        }
    }
    
    @Slave
    @Override
    public List<AssetInventoryVO> listByFranchiseeId(AssetInventoryRequest assetInventoryRequest) {
        AssetInventoryQueryModel assetInventoryQueryModel = new AssetInventoryQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetInventoryQueryModel);
        assetInventoryQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        List<AssetInventoryVO> rspList = new ArrayList<>();
        
        List<AssetInventoryBO> assetInventoryBOList = assetInventoryMapper.selectListByFranchiseeId(assetInventoryQueryModel);
        if (CollectionUtils.isNotEmpty(assetInventoryBOList)) {
            rspList = assetInventoryBOList.stream().map(item -> {
                
                AssetInventoryVO assetInventoryVO = new AssetInventoryVO();
                BeanUtils.copyProperties(item, assetInventoryVO);
    
                Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
                assetInventoryVO.setFranchiseeName(franchisee.getName());
                
                return assetInventoryVO;
            }).collect(Collectors.toList());
        }
        
        return rspList;
    }
    
    @Slave
    @Override
    public Integer countTotal(AssetInventoryRequest assetInventoryRequest) {
        //模型转换
        AssetInventoryQueryModel assetInventoryQueryModel = new AssetInventoryQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetInventoryQueryModel);
        assetInventoryQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        return assetInventoryMapper.countTotal(assetInventoryQueryModel);
    }
    
    @Slave
    @Override
    public AssetInventoryVO queryById(Long id) {
        AssetInventoryVO assetInventoryVO = new AssetInventoryVO();
        AssetInventoryBO assetInventoryBO = assetInventoryMapper.selectById(id);
        if (Objects.nonNull(assetInventoryBO)) {
            BeanUtils.copyProperties(assetInventoryBO, assetInventoryVO);
        }
        return assetInventoryVO;
    }
    
    @Slave
    @Override
    public Integer queryInventoryStatusByFranchiseeId(Long franchiseeId, Integer type) {
        
        return assetInventoryMapper.selectInventoryStatusByFranchiseeId(TenantContextHolder.getTenantId(), franchiseeId, type);
    }
    
    @Override
    public Integer updateByOrderNo(AssetInventoryUpdateDataQueryModel assetInventoryUpdateDataQueryModel) {
        return assetInventoryMapper.updateByOrderNo(assetInventoryUpdateDataQueryModel);
    }
    
    @Slave
    @Override
    public AssetInventoryVO queryByOrderNo(AssetInventoryQueryModel assetInventoryQueryModel) {
        return assetInventoryMapper.selectByOrderNo(assetInventoryQueryModel);
    }
    
}
