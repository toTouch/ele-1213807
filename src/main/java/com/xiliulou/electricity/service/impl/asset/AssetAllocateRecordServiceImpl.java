package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.mapper.asset.AssetAllocateDetailMapper;
import com.xiliulou.electricity.mapper.asset.AssetAllocateRecordMapper;
import com.xiliulou.electricity.query.ElectricityCarMoveQuery;
import com.xiliulou.electricity.queryModel.asset.AssetAllocateRecordSaveQueryModel;
import com.xiliulou.electricity.request.asset.AssetAllocateRecordCabinetRequest;
import com.xiliulou.electricity.request.asset.AssetAllocateRecordRequest;
import com.xiliulou.electricity.request.asset.AssetAllocateRecordSaveRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetBatchUpdateFranchiseeAndStoreRequest;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.asset.AssetAllocateRecordService;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 资产调拨服务
 * @date 2023/11/29 13:33:48
 */
@Slf4j
@Service
public class AssetAllocateRecordServiceImpl implements AssetAllocateRecordService {
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private ElectricityCarService electricityCarService;
    
    @Autowired
    private AssetAllocateRecordMapper assetAllocateRecordMapper;
    
    @Autowired
    private AssetAllocateDetailMapper assetAllocateDetailMapper;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Autowired
    private StoreService storeService;
    
    @Autowired
    private ElectricityCabinetV2Service electricityCabinetV2Service;
    
    
    @Override
    public R save(AssetAllocateRecordRequest assetAllocateRecordRequest, Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_ALLOCATE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            Integer type = assetAllocateRecordRequest.getType();
            // 车辆调拨 复用已实现的 车辆转移功能
            if (Objects.equals(AssetTypeEnum.ASSET_TYPE_CAR.getCode(), type)) {
                ElectricityCarMoveQuery electricityCarMoveQuery = new ElectricityCarMoveQuery();
                electricityCarMoveQuery.setSourceSid(assetAllocateRecordRequest.getSourceStoreId());
                electricityCarMoveQuery.setTargetSid(assetAllocateRecordRequest.getTargetStoreId());
                electricityCarMoveQuery.setCarIds(assetAllocateRecordRequest.getIdList());
                electricityCarMoveQuery.setRemark(assetAllocateRecordRequest.getRemark());
                electricityCarService.electricityCarMove(electricityCarMoveQuery);
            } else {
                // 电池调拨/电柜调拨
                Integer tenantId = TenantContextHolder.getTenantId();
                
                Franchisee sourceFranchisee = franchiseeService.queryByIdFromCache(assetAllocateRecordRequest.getSourceFranchiseeId());
                if (Objects.isNull(sourceFranchisee)) {
                    log.error("ASSET_ALLOCATE ERROR! not found source franchise！franchiseId={}", assetAllocateRecordRequest.getSourceFranchiseeId());
                    return R.fail("ELECTRICITY.0038", "未找到加盟商");
                }
                
                Franchisee targetFranchisee = franchiseeService.queryByIdFromCache(assetAllocateRecordRequest.getTargetFranchiseeId());
                if (Objects.isNull(targetFranchisee)) {
                    log.error("ASSET_ALLOCATE ERROR! not found target franchise! franchiseId={}", assetAllocateRecordRequest.getSourceFranchiseeId());
                    return R.fail("ELECTRICITY.0038", "未找到加盟商");
                }
                
                if (Objects.equals(assetAllocateRecordRequest.getSourceFranchiseeId(), assetAllocateRecordRequest.getTargetFranchiseeId())) {
                    log.error("ASSET_ALLOCATE ERROR! same franchisee! sourceFranchiseeId={}, targetFranchiseeId={}", assetAllocateRecordRequest.getSourceFranchiseeId(),
                            assetAllocateRecordRequest.getTargetFranchiseeId());
                    return R.fail("300809", "调出加盟商与调入加盟商不能相同");
                }
                
                if (!Objects.equals(targetFranchisee.getTenantId(), tenantId) || !Objects.equals(sourceFranchisee.getTenantId(), tenantId)) {
                    return R.ok();
                }
                
                Store sourceStore = storeService.queryByIdFromCache(assetAllocateRecordRequest.getSourceStoreId());
                Store targetStore = storeService.queryByIdFromCache(assetAllocateRecordRequest.getTargetStoreId());
                
                // 电柜调拨
                if (Objects.equals(AssetTypeEnum.ASSET_TYPE_CABINET.getCode(), type)) {
                    List<AssetAllocateRecordCabinetRequest> cabinetList = assetAllocateRecordRequest.getCabinetList();
                    if (CollectionUtils.isNotEmpty(cabinetList) && cabinetList.size() > AssetConstant.ASSET_ALLOCATE_LIMIT_NUMBER) {
                        return R.fail("300811", "资产调拨数量过多");
                    }
                    
                    if (Objects.isNull(sourceStore)) {
                        log.error("ASSET_ALLOCATE ERROR! not found source store！storeId={}", assetAllocateRecordRequest.getSourceStoreId());
                        return R.fail("ELECTRICITY.0018", "未找到门店");
                    }
                    
                    if (Objects.isNull(targetStore)) {
                        log.error("ASSET_ALLOCATE ERROR! not target store！storeId={}", assetAllocateRecordRequest.getTargetStoreId());
                        return R.fail("ELECTRICITY.0018", "未找到门店");
                    }
                    
                    if (Objects.equals(assetAllocateRecordRequest.getSourceStoreId(), assetAllocateRecordRequest.getTargetStoreId())) {
                        log.error("ASSET_ALLOCATE ERROR! same store! sourceStoreId={}, targetStoreId={}", assetAllocateRecordRequest.getSourceStoreId(),
                                assetAllocateRecordRequest.getTargetStoreId());
                        return R.fail("300810", "调出门店与调入门店不能相同");
                    }
                    
                    if (!Objects.equals(targetStore.getTenantId(), tenantId) || !Objects.equals(sourceStore.getTenantId(), tenantId)) {
                        return R.ok();
                    }
    
                    Franchisee storeFranchisee = franchiseeService.queryByIdFromCache(targetStore.getFranchiseeId());
                    if (Objects.isNull(storeFranchisee)) {
                        log.error("ASSET_ALLOCATE ERROR! not found store's franchisee! franchiseeId={}", targetStore.getFranchiseeId());
                        return R.fail("ELECTRICITY.0038", "未找到加盟商");
                    }
    
                    List<ElectricityCabinetBatchUpdateFranchiseeAndStoreRequest> batchUpdateFranchiseeAndStoreRequestList = cabinetList.stream()
                            .map(item -> ElectricityCabinetBatchUpdateFranchiseeAndStoreRequest.builder().id(item.getId()).tenantId(tenantId).targetFranchiseeId(targetStore.getFranchiseeId())
                                    .targetStoreId(targetStore.getId()).sourceFranchiseeId(sourceFranchisee.getId()).sourceStoreId(sourceStore.getId()).build())
                            .collect(Collectors.toList());
    
                    if (CollectionUtils.isNotEmpty(batchUpdateFranchiseeAndStoreRequestList)) {
                        electricityCabinetV2Service.batchUpdateFranchiseeIdAndStoreId(batchUpdateFranchiseeAndStoreRequestList);
                        
                    }
    
                } else {
                    //电池调拨
                    List<Long> idList = assetAllocateRecordRequest.getIdList();
                    if (CollectionUtils.isNotEmpty(idList) && idList.size() > AssetConstant.ASSET_ALLOCATE_LIMIT_NUMBER) {
                        return R.fail("300811", "资产调拨数量过多");
                    }
    
                }
                
                return null;
            }
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_ALLOCATE_LOCK + uid);
        }
        return null;
    }
    
    @Override
    public Integer insertOne(AssetAllocateRecordSaveRequest assetAllocateRecordSaveRequest) {
        AssetAllocateRecordSaveQueryModel assetAllocateRecordSaveQueryModel = new AssetAllocateRecordSaveQueryModel();
        BeanUtils.copyProperties(assetAllocateRecordSaveRequest, assetAllocateRecordSaveQueryModel);
        
        return assetAllocateRecordMapper.insertOne(assetAllocateRecordSaveQueryModel);
    }
}
