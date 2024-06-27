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
import com.xiliulou.electricity.query.asset.AssetInventoryQueryModel;
import com.xiliulou.electricity.query.asset.AssetInventorySaveOrUpdateQueryModel;
import com.xiliulou.electricity.query.asset.AssetInventoryUpdateDataQueryModel;
import com.xiliulou.electricity.request.asset.AssetInventoryRequest;
import com.xiliulou.electricity.request.asset.AssetInventorySaveOrUpdateRequest;
import com.xiliulou.electricity.request.asset.ElectricityBatterySnSearchRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.service.asset.AssetInventoryDetailService;
import com.xiliulou.electricity.service.asset.AssetInventoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.asset.AssetInventoryVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 资产盘点业务
 * @date 2023/11/20 13:26:02
 */
@Slf4j
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
    
    @Autowired
    private AssertPermissionService assertPermissionService;
    
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
    
            Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
            if (Objects.isNull(franchisee)) {
                log.error("ASSET_INVENTORY ERROR! not found franchise! franchiseId={}", assetInventorySaveOrUpdateRequest.getFranchiseeId());
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
    
            // tenantId校验
            if (!Objects.equals(franchisee.getTenantId(), tenantId)) {
                return R.ok();
            }
            
            // 默认资产类型是电池,获取查询电池数量
            ElectricityBatteryQuery electricityBatteryQuery = ElectricityBatteryQuery.builder().tenantId(tenantId).franchiseeId(franchiseeId).build();
            Object data = electricityBatteryService.queryCount(electricityBatteryQuery).getData();
    
            // 生成资产盘点订单
            Integer insert = NumberConstant.ZERO;
            if (Objects.nonNull(data) && (Integer) data > NumberConstant.ZERO) {
                AssetInventorySaveOrUpdateQueryModel assetInventorySaveOrUpdateQueryModel = AssetInventorySaveOrUpdateQueryModel.builder().orderNo(orderNo)
                        .franchiseeId(franchiseeId).type(AssetTypeEnum.ASSET_TYPE_BATTERY.getCode()).status(AssetConstant.ASSET_INVENTORY_STATUS_TAKING)
                        .inventoriedTotal(NumberConstant.ZERO).pendingTotal((Integer) data).finishTime(assetInventorySaveOrUpdateRequest.getFinishTime()).operator(operator)
                        .tenantId(tenantId).delFlag(AssetConstant.DEL_NORMAL).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        
                insert = assetInventoryMapper.insertOne(assetInventorySaveOrUpdateQueryModel);
            }
    
            //异步记录
            if (insert > NumberConstant.ZERO) {
                ElectricityBatterySnSearchRequest snSearchRequest = ElectricityBatterySnSearchRequest.builder().tenantId(tenantId).franchiseeId(franchiseeId).build();
                executorService.execute(() -> {
                    assetInventoryDetailService.asyncBatteryProcess(snSearchRequest, orderNo, operator);
                });
            }
            
            return R.ok(insert);
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_INVENTORY_LOCK + operator);
        }
    }
    
    @Slave
    @Override
    public List<AssetInventoryVO> listByPage(AssetInventoryRequest assetInventoryRequest) {
        AssetInventoryQueryModel assetInventoryQueryModel = new AssetInventoryQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetInventoryQueryModel);
        assetInventoryQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        List<AssetInventoryVO> rspList = Collections.emptyList();
        
        // 加盟商权限
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()){
            return Collections.emptyList();
        }
        assetInventoryQueryModel.setFranchiseeIds(pair.getRight());
        
        List<AssetInventoryBO> assetInventoryBOList = assetInventoryMapper.selectListByFranchiseeId(assetInventoryQueryModel);
        if (CollectionUtils.isNotEmpty(assetInventoryBOList)) {
            rspList = assetInventoryBOList.stream().map(item -> {
                
                AssetInventoryVO assetInventoryVO = new AssetInventoryVO();
                BeanUtils.copyProperties(item, assetInventoryVO);
                
                if (Objects.nonNull(item.getFranchiseeId())) {
                    assetInventoryVO.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(item.getFranchiseeId())).orElse(new Franchisee()).getName());
                }
                
                return assetInventoryVO;
            }).collect(Collectors.toList());
        }
    
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
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
        
        // 加盟商权限
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()){
            return NumberConstant.ZERO;
        }
        assetInventoryQueryModel.setFranchiseeIds(pair.getRight());
        
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
    
    @Slave
    @Override
    public Integer existInventoryByFranchiseeIdList(List<Long> franchiseeIdList, Integer type) {
        return assetInventoryMapper.existInventoryByFranchiseeIdList(TenantContextHolder.getTenantId(), franchiseeIdList, type);
    }
}
