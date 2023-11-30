package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.asset.AssetWarehouseBO;
import com.xiliulou.electricity.bo.asset.AssetWarehouseNameBO;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.mapper.asset.AssetWarehouseMapper;
import com.xiliulou.electricity.queryModel.asset.AssetWarehouseQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetWarehouseSaveOrUpdateQueryModel;
import com.xiliulou.electricity.request.asset.AssetWarehouseRequest;
import com.xiliulou.electricity.request.asset.AssetWarehouseSaveOrUpdateRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.asset.AssetWarehouseService;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.asset.AssetWarehouseNameVO;
import com.xiliulou.electricity.vo.asset.AssetWarehouseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    
    @Override
    public R save(AssetWarehouseSaveOrUpdateRequest assetWarehouseSaveOrUpdateRequest, Long uid) {
    
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_WAREHOUSE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
    
        try {
            Integer exists = existsByName(assetWarehouseSaveOrUpdateRequest.getName());
            if (Objects.nonNull(exists)) {
                return R.fail("300803", "库房名称已存在");
            }
        
            AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel = AssetWarehouseSaveOrUpdateQueryModel.builder().name(assetWarehouseSaveOrUpdateRequest.getName())
                    .status(assetWarehouseSaveOrUpdateRequest.getStatus()).managerName(assetWarehouseSaveOrUpdateRequest.getManagerName())
                    .managerPhone(assetWarehouseSaveOrUpdateRequest.getManagerPhone()).address(assetWarehouseSaveOrUpdateRequest.getAddress()).delFlag(AssetConstant.DEL_NORMAL)
                    .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).tenantId(TenantContextHolder.getTenantId()).build();
        
            return R.ok(assetWarehouseMapper.insertOne(warehouseSaveOrUpdateQueryModel));
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_WAREHOUSE_LOCK + uid);
        }
    }
    
    @Slave
    @Override
    public List<AssetWarehouseVO> listByFranchiseeId(AssetWarehouseRequest assetInventoryRequest) {
        
        AssetWarehouseQueryModel assetWarehouseQueryModel = new AssetWarehouseQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetWarehouseQueryModel);
        assetWarehouseQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        List<AssetWarehouseVO> rspList = Collections.emptyList();
        List<AssetWarehouseBO> assetWarehouseBOList = assetWarehouseMapper.selectListByFranchiseeId(assetWarehouseQueryModel);
        if (CollectionUtils.isNotEmpty(assetWarehouseBOList)) {
            rspList = assetWarehouseBOList.stream().map(item -> {
                AssetWarehouseVO assetWarehouseVO = new AssetWarehouseVO();
                BeanUtils.copyProperties(item, assetWarehouseVO);
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
        AssetWarehouseNameBO assetWarehouseNameBO = assetWarehouseMapper.selectById(id);
        if (Objects.nonNull(assetWarehouseNameBO)) {
            BeanUtils.copyProperties(assetWarehouseNameBO, assetWarehouseNameVO);
        }
        return assetWarehouseNameVO;
    }
    
    @Override
    public R deleteById(Long id) {
        // 判断库房是否绑定柜机
        Integer existsElectricityCabinet = electricityCabinetV2Service.existsByWarehouseId(id);
        if (Objects.nonNull(existsElectricityCabinet)) {
            return R.fail("300800", "该库房有电柜正在使用,请先解绑后操作");
        }
        
        // 判断库房是否绑定电池
        Integer existsElectricityBattery = electricityBatteryService.existsByWarehouseId(id);
        if (Objects.nonNull(existsElectricityBattery)) {
            return R.fail("300801", "该库房有电池正在使用,请先解绑后操作");
        }
        
        // 判断库房是否绑定车辆 TODO
        
        
        AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel = AssetWarehouseSaveOrUpdateQueryModel.builder().id(id).delFlag(AssetConstant.DEL_DEL)
                .updateTime(System.currentTimeMillis()).tenantId(TenantContextHolder.getTenantId()).build();
        
        return R.ok(assetWarehouseMapper.updateById(warehouseSaveOrUpdateQueryModel));
    }
    
    @Override
    public R updateById(AssetWarehouseSaveOrUpdateRequest assetWarehouseSaveOrUpdateRequest) {
        AssetWarehouseNameBO assetWarehouseNameBO = assetWarehouseMapper.selectById(assetWarehouseSaveOrUpdateRequest.getId());
        if (Objects.nonNull(assetWarehouseNameBO) && !Objects.equals(assetWarehouseNameBO.getName(), assetWarehouseSaveOrUpdateRequest.getName())) {
            Integer exists = existsByName(assetWarehouseSaveOrUpdateRequest.getName());
            if (Objects.nonNull(exists)) {
                return R.fail("300803", "库房名称已存在");
            }
        }
        
        AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel = new AssetWarehouseSaveOrUpdateQueryModel();
        BeanUtils.copyProperties(assetWarehouseSaveOrUpdateRequest, warehouseSaveOrUpdateQueryModel);
        warehouseSaveOrUpdateQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        return R.ok(assetWarehouseMapper.updateById(warehouseSaveOrUpdateQueryModel));
    }
    
    @Slave
    @Override
    public Integer existsByName(String name) {
        return assetWarehouseMapper.existsByName(TenantContextHolder.getTenantId(), name);
    }
    
    
    
}
