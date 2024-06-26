package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.asset.AssetWarehouseBO;
import com.xiliulou.electricity.bo.asset.AssetWarehouseNameBO;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.enums.asset.StockStatusEnum;
import com.xiliulou.electricity.mapper.asset.AssetWarehouseMapper;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.query.ElectricityCarQuery;
import com.xiliulou.electricity.query.asset.AssetWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.AssetWarehouseSaveOrUpdateQueryModel;
import com.xiliulou.electricity.request.asset.AssetWarehouseRequest;
import com.xiliulou.electricity.request.asset.AssetWarehouseSaveOrUpdateRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.service.asset.AssetWarehouseService;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.asset.AssetWarehouseNameVO;
import com.xiliulou.electricity.vo.asset.AssetWarehouseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 库房业务
 * @date 2023/11/21 16:04:01
 */

@Service
public class AssetWarehouseServiceImpl implements AssetWarehouseService {
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private AssetWarehouseMapper assetWarehouseMapper;
    
    @Autowired
    private ElectricityCabinetV2Service electricityCabinetV2Service;
    
    @Autowired
    private ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    private ElectricityCarService electricityCarService;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private AssertPermissionService assertPermissionService;
    
    @Override
    public R save(AssetWarehouseSaveOrUpdateRequest assetWarehouseSaveOrUpdateRequest, Long uid) {
        
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_WAREHOUSE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            Integer exists = existsByName(assetWarehouseSaveOrUpdateRequest.getName());
            if (Objects.nonNull(exists)) {
                return R.fail("300803", "库房名称重复，请修改后操作");
            }
            
            AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel = AssetWarehouseSaveOrUpdateQueryModel.builder().name(assetWarehouseSaveOrUpdateRequest.getName())
                    .status(assetWarehouseSaveOrUpdateRequest.getStatus()).managerName(assetWarehouseSaveOrUpdateRequest.getManagerName())
                    .managerPhone(assetWarehouseSaveOrUpdateRequest.getManagerPhone()).address(assetWarehouseSaveOrUpdateRequest.getAddress()).delFlag(AssetConstant.DEL_NORMAL)
                    .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).tenantId(TenantContextHolder.getTenantId())
                    .franchiseeId(assetWarehouseSaveOrUpdateRequest.getFranchiseeId()).build();
            
            return R.ok(assetWarehouseMapper.insertOne(warehouseSaveOrUpdateQueryModel));
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_WAREHOUSE_LOCK + uid);
        }
    }
    
    @Slave
    @Override
    public List<AssetWarehouseVO> listByPage(AssetWarehouseRequest assetInventoryRequest) {
        
        AssetWarehouseQueryModel assetWarehouseQueryModel = new AssetWarehouseQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetWarehouseQueryModel);
        assetWarehouseQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        // 加盟商权限
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()){
            return Collections.emptyList();
        }
        assetWarehouseQueryModel.setFranchiseeIds(pair.getRight());
        
        List<AssetWarehouseVO> rspList = Collections.emptyList();
        List<AssetWarehouseBO> assetWarehouseBOList = assetWarehouseMapper.selectListByPage(assetWarehouseQueryModel);
        if (CollectionUtils.isNotEmpty(assetWarehouseBOList)) {
            
            rspList = assetWarehouseBOList.stream().map(item -> {
                AssetWarehouseVO assetWarehouseVO = new AssetWarehouseVO();
                BeanUtils.copyProperties(item, assetWarehouseVO);
                
                // 统计电池数量
                ElectricityBatteryQuery electricityBatteryQuery = ElectricityBatteryQuery.builder().tenantId(item.getTenantId()).warehouseId(item.getId())
                        .stockStatus(StockStatusEnum.STOCK.getCode()).build();
                Integer batteryCount = (Integer) electricityBatteryService.queryCount(electricityBatteryQuery).getData();
                
                // 统计柜机数量
                ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().tenantId(item.getTenantId()).warehouseId(item.getId())
                        .stockStatus(StockStatusEnum.STOCK.getCode()).build();
                Integer cabinetCount = (Integer) electricityCabinetService.queryCount(electricityCabinetQuery).getData();
                
                // 统计车辆数量
                ElectricityCarQuery electricityCarQuery = ElectricityCarQuery.builder().tenantId(item.getTenantId()).warehouseId(item.getId())
                        .stockStatus(StockStatusEnum.STOCK.getCode()).build();
                Integer carCount = (Integer) electricityCarService.queryCountByWarehouse(electricityCarQuery).getData();
                
                Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
                assetWarehouseVO.setFranchiseeId(item.getFranchiseeId());
                if (Objects.nonNull(franchisee)){
                    assetWarehouseVO.setFranchiseeName(franchisee.getName());
                }
                
                assetWarehouseVO.setBatteryCount(batteryCount);
                assetWarehouseVO.setCabinetCount(cabinetCount);
                assetWarehouseVO.setCarCount(carCount);
                
                return assetWarehouseVO;
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
        
        return rspList;
    }
    
    @Slave
    public Integer countTotal(AssetWarehouseRequest assetInventoryRequest) {
        AssetWarehouseQueryModel assetWarehouseQueryModel = new AssetWarehouseQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetWarehouseQueryModel);
        assetWarehouseQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        // 加盟商权限
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()){
            return NumberUtils.INTEGER_ZERO;
        }
        assetWarehouseQueryModel.setFranchiseeIds(pair.getRight());
        
        return assetWarehouseMapper.countTotal(assetWarehouseQueryModel);
    }
    
    @Slave
    @Override
    public List<AssetWarehouseNameVO> listWarehouseNames(AssetWarehouseRequest assetInventoryRequest) {
        AssetWarehouseQueryModel assetWarehouseQueryModel = new AssetWarehouseQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetWarehouseQueryModel);
        assetWarehouseQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        List<AssetWarehouseNameVO> rspList = Collections.emptyList();
        
        List<AssetWarehouseNameBO> assetWarehouseNameBOList = assetWarehouseMapper.selectListWarehouseNames(assetWarehouseQueryModel);
        if (CollectionUtils.isNotEmpty(assetWarehouseNameBOList)) {
            rspList = assetWarehouseNameBOList.stream().map(item -> {
                
                AssetWarehouseNameVO assetWarehouseNameVO = new AssetWarehouseNameVO();
                BeanUtils.copyProperties(item, assetWarehouseNameVO);
                
                return assetWarehouseNameVO;
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
        
        return rspList;
    }
    
    @Slave
    @Override
    public AssetWarehouseNameVO queryById(Long id) {
        AssetWarehouseNameVO assetWarehouseNameVO = new AssetWarehouseNameVO();
        AssetWarehouseBO assetWarehouseBO = assetWarehouseMapper.selectById(id);
        if (Objects.nonNull(assetWarehouseBO)) {
            BeanUtils.copyProperties(assetWarehouseBO, assetWarehouseNameVO);
        }
        return assetWarehouseNameVO;
    }
    
    @Override
    public R deleteById(Long id) {
        // 判断库房是否绑定库存状态的柜机（不需要校验出库的，只校验库存状态）
        Integer existsElectricityCabinet = electricityCabinetV2Service.existsByWarehouseId(id);
        if (Objects.nonNull(existsElectricityCabinet)) {
            return R.fail("300800", "该库房有电柜正在使用,请先解绑后操作");
        }
        
        // 判断库房是否绑定库存状态的电池（不需要校验出库的，只校验库存状态）
        Integer existsElectricityBattery = electricityBatteryService.existsByWarehouseId(id);
        if (Objects.nonNull(existsElectricityBattery)) {
            return R.fail("300801", "该库房有电池正在使用,请先解绑后操作");
        }
        
        // 判断库房是否绑定库存状态的车辆（不需要校验出库的，只校验库存状态）
        Integer existsElectricityCar = electricityCarService.existsByWarehouseId(id);
        if (Objects.nonNull(existsElectricityCar)) {
            return R.fail("300802", "该库房有车辆正在使用,请先解绑后操作");
        }
        
        // 根据id查库房
        AssetWarehouseNameVO assetWarehouseNameVO = queryById(id);
        if (Objects.nonNull(assetWarehouseNameVO) && !Objects.equals(assetWarehouseNameVO.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }
        
        AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel = AssetWarehouseSaveOrUpdateQueryModel.builder().id(id).delFlag(AssetConstant.DEL_DEL)
                .updateTime(System.currentTimeMillis()).tenantId(TenantContextHolder.getTenantId()).build();
        
        return R.ok(assetWarehouseMapper.updateById(warehouseSaveOrUpdateQueryModel));
    }
    
    @Override
    public R updateById(AssetWarehouseSaveOrUpdateRequest assetWarehouseSaveOrUpdateRequest) {
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_WAREHOUSE_UPDATE_LOCK + assetWarehouseSaveOrUpdateRequest.getId(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            AssetWarehouseBO assetWarehouseBO = assetWarehouseMapper.selectById(assetWarehouseSaveOrUpdateRequest.getId());
            if (Objects.nonNull(assetWarehouseBO) && !Objects.equals(assetWarehouseBO.getName(), assetWarehouseSaveOrUpdateRequest.getName())) {
                Integer exists = existsByName(assetWarehouseSaveOrUpdateRequest.getName());
                if (Objects.nonNull(exists)) {
                    return R.fail("300803", "库房名称重复，请修改后操作");
                }
                
                if (!Objects.equals(assetWarehouseBO.getTenantId(), TenantContextHolder.getTenantId())) {
                    return R.ok();
                }
            }
            
            AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel = new AssetWarehouseSaveOrUpdateQueryModel();
            BeanUtils.copyProperties(assetWarehouseSaveOrUpdateRequest, warehouseSaveOrUpdateQueryModel);
            warehouseSaveOrUpdateQueryModel.setTenantId(TenantContextHolder.getTenantId());
            warehouseSaveOrUpdateQueryModel.setUpdateTime(System.currentTimeMillis());
            
            return R.ok(assetWarehouseMapper.updateById(warehouseSaveOrUpdateQueryModel));
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_WAREHOUSE_UPDATE_LOCK + assetWarehouseSaveOrUpdateRequest.getId());
        }
    }
    
    @Slave
    @Override
    public Integer existsByName(String name) {
        return assetWarehouseMapper.existsByName(TenantContextHolder.getTenantId(), name);
    }
    
    @Slave
    @Override
    public List<AssetWarehouseNameVO> selectByIdList(List<Long> list) {
        List<AssetWarehouseNameVO> resultList = Collections.emptyList();
        List<Long> idList = list.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return resultList;
        }
        List<AssetWarehouseNameBO> assetWarehouseNameBOList = assetWarehouseMapper.selectListByIdList(TenantContextHolder.getTenantId(), idList);
        if (CollectionUtils.isNotEmpty(assetWarehouseNameBOList)) {
            resultList = assetWarehouseNameBOList.stream().map(item -> {
                AssetWarehouseNameVO assetWarehouseNameVO = new AssetWarehouseNameVO();
                BeanUtils.copyProperties(item, assetWarehouseNameVO);
                
                return assetWarehouseNameVO;
            }).collect(Collectors.toList());
        }
        
        return resultList;
    }
}
