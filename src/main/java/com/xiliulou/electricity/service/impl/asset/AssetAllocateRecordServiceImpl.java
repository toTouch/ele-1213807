package com.xiliulou.electricity.service.impl.asset;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.asset.AssetAllocateRecordBO;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.CarModelTag;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Picture;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.mapper.asset.AssetAllocateRecordMapper;
import com.xiliulou.electricity.query.PictureQuery;
import com.xiliulou.electricity.query.asset.AssetAllocateRecordPageQueryModel;
import com.xiliulou.electricity.query.asset.AssetAllocateRecordSaveQueryModel;
import com.xiliulou.electricity.request.asset.AssetAllocateDetailSaveRequest;
import com.xiliulou.electricity.request.asset.AssetAllocateRecordPageRequest;
import com.xiliulou.electricity.request.asset.AssetAllocateRecordRequest;
import com.xiliulou.electricity.request.asset.AssetAllocateRecordSaveRequest;
import com.xiliulou.electricity.request.asset.ElectricityBatteryBatchUpdateFranchiseeRequest;
import com.xiliulou.electricity.request.asset.ElectricityBatteryEnableAllocateRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetBatchUpdateFranchiseeAndStoreRequest;
import com.xiliulou.electricity.request.asset.ElectricityCarBatchUpdateFranchiseeAndStoreRequest;
import com.xiliulou.electricity.service.CarModelTagService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.PictureService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.service.asset.AssetAllocateDetailService;
import com.xiliulou.electricity.service.asset.AssetAllocateRecordService;
import com.xiliulou.electricity.service.asset.AssetInventoryService;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import com.xiliulou.electricity.vo.asset.AssetAllocateDetailVO;
import com.xiliulou.electricity.vo.asset.AssetAllocateRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 资产调拨服务
 * @date 2023/11/29 13:33:48
 */
@Slf4j
@Service
public class AssetAllocateRecordServiceImpl implements AssetAllocateRecordService {
    
    protected XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("ASSET_ALLOCATE_RECORD_HANDLE_THREAD_POOL", 3,
            "asset_allocate_record_handle_thread_pool");
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private ElectricityCarService electricityCarService;
    
    @Autowired
    private AssetAllocateRecordMapper assetAllocateRecordMapper;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Autowired
    private StoreService storeService;
    
    @Autowired
    private ElectricityCabinetV2Service electricityCabinetV2Service;
    
    @Autowired
    private ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    private AssetInventoryService assetInventoryService;
    
    @Autowired
    private ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    private AssetAllocateDetailService assetAllocateDetailService;
    
    @Autowired
    private ElectricityCarModelService electricityCarModelService;
    
    @Autowired
    private CarModelTagService carModelTagService;
    
    @Autowired
    private PictureService pictureService;
    
    @Autowired
    private AssertPermissionService assertPermissionService;
    
    @Override
    public R save(AssetAllocateRecordRequest assetAllocateRecordRequest, Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_ALLOCATE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            Integer type = assetAllocateRecordRequest.getType();
            // 盘点盘点状态
            R inventoryStatus = judgeInventoryStatus(assetAllocateRecordRequest.getSourceFranchiseeId(), type);
            if (!inventoryStatus.isSuccess()) {
                return R.fail(inventoryStatus.getErrCode(), inventoryStatus.getErrMsg());
            }
            
            List<String> exitsSn = new ArrayList<>();
            List<Long> idList = assetAllocateRecordRequest.getIdList();
            
            //根据sn查询
            if (Objects.equals(assetAllocateRecordRequest.getSubmitType(), AssetConstant.ASSET_EXIT_WAREHOUSE_SUBMIT_TYPE_BY_SN)) {
                List<String> snList = assetAllocateRecordRequest.getSnList();
                if (CollectionUtils.isEmpty(snList)) {
                    return R.ok();
                }
                
                snList = assetAllocateRecordRequest.getSnList().stream().distinct().collect(Collectors.toList());
                if (snList.size() > AssetConstant.ASSET_ALLOCATE_LIMIT_NUMBER) {
                    return R.fail("300811", "资产调拨数量最大限制50条，请修改");
                }
                
                Map<String, Long> map = queryIdBasedOnTypeAndSNCode(assetAllocateRecordRequest);
                for (String s : snList) {
                    if (!map.containsKey(s)) {
                        exitsSn.add(s);
                    }
                }
                
                if (CollectionUtils.isNotEmpty(exitsSn)) {
                    return R.fail("300832", String.format("您输入的编号为[%s]，系统未能找到对应的信息，请您核实并修改后提交", String.join(",", exitsSn)));
                }
                
                idList = ListUtil.toList(map.values());
            }
            
            if (CollectionUtils.isEmpty(idList)) {
                return R.ok();
            }
            
            if (idList.size() > AssetConstant.ASSET_ALLOCATE_LIMIT_NUMBER) {
                return R.fail("300811", "资产调拨数量最大限制50条，请修改");
            }
            
            Integer tenantId = TenantContextHolder.getTenantId();
            Franchisee sourceFranchisee = franchiseeService.queryByIdFromCache(assetAllocateRecordRequest.getSourceFranchiseeId());
            if (Objects.isNull(sourceFranchisee)) {
                log.error("ASSET_ALLOCATE ERROR! not found source franchise! franchiseId={}", assetAllocateRecordRequest.getSourceFranchiseeId());
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
            
            Franchisee targetFranchisee = franchiseeService.queryByIdFromCache(assetAllocateRecordRequest.getTargetFranchiseeId());
            if (Objects.isNull(targetFranchisee)) {
                log.error("ASSET_ALLOCATE ERROR! not found target franchise! franchiseId={}", assetAllocateRecordRequest.getSourceFranchiseeId());
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
            
            if (!Objects.equals(sourceFranchisee.getTenantId(), tenantId) || !Objects.equals(targetFranchisee.getTenantId(), tenantId)) {
                return R.ok();
            }
            
            if (Objects.equals(AssetTypeEnum.ASSET_TYPE_CAR.getCode(), type) || Objects.equals(AssetTypeEnum.ASSET_TYPE_CABINET.getCode(), type)) {
                if (Objects.equals(assetAllocateRecordRequest.getSourceStoreId(), assetAllocateRecordRequest.getTargetStoreId())) {
                    log.error("ASSET_ALLOCATE ERROR! same store! sourceStoreId={}, targetStoreId={}", assetAllocateRecordRequest.getSourceStoreId(),
                            assetAllocateRecordRequest.getTargetStoreId());
                    return R.fail("300810", "调出门店与调入门店不能相同，请修改");
                }
                
                if (Objects.isNull(assetAllocateRecordRequest.getSourceStoreId())) {
                    log.error("ASSET_ALLOCATE ERROR! not found source storeId!");
                    return R.fail("ELECTRICITY.0018", "未找到门店");
                }
                
                if (Objects.isNull(assetAllocateRecordRequest.getTargetStoreId())) {
                    log.error("ASSET_ALLOCATE ERROR! not found target storeId!");
                    return R.fail("ELECTRICITY.0018", "未找到门店");
                }
                
                Store sourceStore = storeService.queryByIdFromCache(assetAllocateRecordRequest.getSourceStoreId());
                Store targetStore = storeService.queryByIdFromCache(assetAllocateRecordRequest.getTargetStoreId());
                if (Objects.isNull(sourceStore)) {
                    log.error("ASSET_ALLOCATE ERROR! not found source store! storeId={}", assetAllocateRecordRequest.getSourceStoreId());
                    return R.fail("ELECTRICITY.0018", "未找到门店");
                }
                
                if (Objects.isNull(targetStore)) {
                    log.error("ASSET_ALLOCATE ERROR! not target store! storeId={}", assetAllocateRecordRequest.getTargetStoreId());
                    return R.fail("ELECTRICITY.0018", "未找到门店");
                }
                
                if (!Objects.equals(targetStore.getTenantId(), TenantContextHolder.getTenantId()) || !Objects.equals(sourceStore.getTenantId(),
                        TenantContextHolder.getTenantId())) {
                    return R.ok();
                }
                
                Franchisee franchisee = franchiseeService.queryByIdFromCache(targetStore.getFranchiseeId());
                if (Objects.isNull(franchisee)) {
                    log.error("ASSET_ALLOCATE ERROR! not found franchisee! franchiseeId={}", targetStore.getFranchiseeId());
                    return R.fail("ELECTRICITY.0038", "未找到加盟商");
                }
                
                //车辆调拨 复用以前的车辆迁移
                if (Objects.equals(AssetTypeEnum.ASSET_TYPE_CAR.getCode(), type)) {
                    return electricityCarMove(assetAllocateRecordRequest, targetStore, franchisee, tenantId, idList, uid);
                } else {
                    // 电柜调拨
                    return electricityCabinetMove(assetAllocateRecordRequest, targetStore, tenantId, idList, uid);
                }
            } else {
                //电池调拨
                //                if (Objects.equals(assetAllocateRecordRequest.getSourceFranchiseeId(), assetAllocateRecordRequest.getTargetFranchiseeId())) {
                //                    log.error("ASSET_ALLOCATE ERROR! same franchisee! sourceFranchiseeId={}, targetFranchiseeId={}", assetAllocateRecordRequest.getSourceFranchiseeId(),
                //                            assetAllocateRecordRequest.getTargetFranchiseeId());
                //                    return R.fail("300809", "调出加盟商与调入加盟商不能相同，请修改");
                //                }
                return electricityBatteryMove(assetAllocateRecordRequest, tenantId, idList, uid);
            }
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_ALLOCATE_LOCK + uid);
        }
    }
    
    /**
     * <p>
     * Description: queryIdBasedOnTypeAndSNCode
     * </p>
     *
     * @param assetAllocateRecordRequest assetAllocateRecordRequest
     * @return java.util.List<java.lang.Long>
     * <p>Project: AssetAllocateRecordServiceImpl</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/18
     */
    private Map<String, Long> queryIdBasedOnTypeAndSNCode(AssetAllocateRecordRequest assetAllocateRecordRequest) {
        List<String> snList = assetAllocateRecordRequest.getSnList().stream().distinct().collect(Collectors.toList());
        Integer type = assetAllocateRecordRequest.getType();
        Map<String, Long> result = null;
        if (Objects.equals(type, AssetTypeEnum.ASSET_TYPE_CAR.getCode())) {
            result = electricityCarService.listIdsBySnArray(snList, TenantContextHolder.getTenantId(), assetAllocateRecordRequest.getSourceFranchiseeId());
        }
        if (Objects.equals(type, AssetTypeEnum.ASSET_TYPE_BATTERY.getCode())) {
            result = electricityBatteryService.listIdsBySnArray(snList, TenantContextHolder.getTenantId(), assetAllocateRecordRequest.getSourceFranchiseeId());
        }
        if (Objects.equals(type, AssetTypeEnum.ASSET_TYPE_CABINET.getCode())) {
            result = electricityCabinetService.listIdsBySnArray(snList, TenantContextHolder.getTenantId(), assetAllocateRecordRequest.getSourceFranchiseeId());
        }
        if (MapUtil.isEmpty(result)) {
            return MapUtil.empty();
        }
        return result;
    }
    
    public R electricityCarMove(AssetAllocateRecordRequest assetAllocateRecordRequest, Store targetStore, Franchisee targetStoreFranchisee, Integer tenantId, List<Long> idList,
            Long uid) {
        List<ElectricityCar> electricityCarList = electricityCarService.queryModelIdBySidAndIds(idList, assetAllocateRecordRequest.getSourceStoreId(),
                ElectricityCar.STATUS_NOT_RENT, TenantContextHolder.getTenantId());
        
        if (CollectionUtils.isEmpty(electricityCarList) || electricityCarList.size() != idList.size()) {
            log.error("ELECTRICITY_CAR_MOVE ERROR! has illegal cars! carIds={}", idList);
            return R.fail("300815", "您选择的车辆编码中存在不可调拨的数据，请刷新页面以获取最新状态后再进行操作");
        }
        
        // 存批量修改的request
        List<ElectricityCarBatchUpdateFranchiseeAndStoreRequest> carBatchUpdateFranchiseeAndStoreRequestList = new ArrayList<>();
        
        Map<Integer, List<ElectricityCar>> collect = electricityCarList.parallelStream().collect(Collectors.groupingBy(ElectricityCar::getModelId));
        collect.forEach((k, v) -> {
            //k --> ModelId  v --> List<ElectricityCar>
            ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(k);
            if (Objects.isNull(electricityCarModel)) {
                log.error("ELECTRICITY_CAR_MOVE ERROR! CarModel is null error! carModel={}", k);
                return;
            }
            
            //如果目标门店没同名类型则要创建
            ElectricityCarModel targetCarModel = electricityCarModelService.queryByNameAndStoreId(electricityCarModel.getName(), targetStore.getId());
            if (Objects.isNull(targetCarModel)) {
                //拷贝类型
                targetCarModel = new ElectricityCarModel();
                BeanUtil.copyProperties(electricityCarModel, targetCarModel);
                targetCarModel.setFranchiseeId(targetStoreFranchisee.getId());
                targetCarModel.setStoreId(targetStore.getId());
                targetCarModel.setUpdateTime(System.currentTimeMillis());
                targetCarModel.setCreateTime(System.currentTimeMillis());
                electricityCarModelService.insert(targetCarModel);
                
                //拷贝标签
                List<CarModelTag> carModelTags = Optional.ofNullable(carModelTagService.selectByCarModelId(electricityCarModel.getId())).orElse(new ArrayList<>());
                for (CarModelTag carModelTag : carModelTags) {
                    carModelTag.setId(null);
                    carModelTag.setCarModelId(targetCarModel.getId().longValue());
                    carModelTag.setCreateTime(System.currentTimeMillis());
                    carModelTag.setUpdateTime(System.currentTimeMillis());
                }
                carModelTagService.batchInsert(carModelTags);
                
                ///拷贝图片
                PictureQuery pictureQuery = new PictureQuery();
                pictureQuery.setBusinessId(electricityCarModel.getId().longValue());
                pictureQuery.setStatus(Picture.STATUS_ENABLE);
                pictureQuery.setImgType(Picture.TYPE_CAR_IMG);
                pictureQuery.setDelFlag(Picture.DEL_NORMAL);
                pictureQuery.setTenantId(tenantId);
                List<Picture> pictures = pictureService.queryListByQuery(pictureQuery);
                for (Picture picture : pictures) {
                    picture.setId(null);
                    picture.setBusinessId(targetCarModel.getId().longValue());
                    picture.setCreateTime(System.currentTimeMillis());
                    picture.setUpdateTime(System.currentTimeMillis());
                }
                pictureService.batchInsert(pictures);
            }
            
            Integer targetCarModelId = targetCarModel.getId();
            
            //修改被迁移车辆门店及类型
            Optional.ofNullable(v).orElse(new ArrayList<>()).parallelStream().forEach(item -> {
                ElectricityCarBatchUpdateFranchiseeAndStoreRequest carBatchUpdateFranchiseeAndStoreRequest = ElectricityCarBatchUpdateFranchiseeAndStoreRequest.builder()
                        .id(item.getId().longValue()).modelId(targetCarModelId).targetFranchiseeId(targetStoreFranchisee.getId()).targetStoreId(targetStore.getId())
                        .tenantId(tenantId).build();
                carBatchUpdateFranchiseeAndStoreRequestList.add(carBatchUpdateFranchiseeAndStoreRequest);
            });
        });
        
        if (CollectionUtils.isNotEmpty(carBatchUpdateFranchiseeAndStoreRequestList)) {
            Integer count = electricityCarService.batchUpdateRemove(carBatchUpdateFranchiseeAndStoreRequestList);
            
            if (!Objects.equals(count, NumberConstant.ZERO)) {
                // 异步记录
                executorService.execute(() -> {
                    saveAllocateRecords(assetAllocateRecordRequest, null, null, electricityCarList, tenantId, uid);
                });
            }
        }
        
        return R.ok();
    }
    
    private R electricityCabinetMove(AssetAllocateRecordRequest assetAllocateRecordRequest, Store targetStore, Integer tenantId, List<Long> idList, Long uid) {
        Franchisee storeFranchisee = franchiseeService.queryByIdFromCache(targetStore.getFranchiseeId());
        if (Objects.isNull(storeFranchisee)) {
            log.error("ELECTRICITY_CABINET_MOVE ERROR! not found store's franchisee! franchiseeId={}", targetStore.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        
        // 根据id集获取柜机信息
        Set<Integer> idSet = (idList.stream().map(Long::intValue).collect(Collectors.toSet()));
        List<ElectricityCabinet> electricityCabinetList = electricityCabinetService.listByIds(idSet);
        
        if (CollectionUtils.isEmpty(electricityCabinetList) || !Objects.equals(idList.size(), electricityCabinetList.size())) {
            log.error("ELECTRICITY_CABINET_MOVE ERROR! has illegal cabinet! idList={}", idList);
            return R.fail("300816", "您选择的电柜编码中存在不可调拨的数据，请刷新页面以获取最新状态后再进行操作");
        }
        
        List<ElectricityCabinetBatchUpdateFranchiseeAndStoreRequest> batchUpdateFranchiseeAndStoreRequestList = electricityCabinetList.stream()
                .map(electricityCabinet -> ElectricityCabinetBatchUpdateFranchiseeAndStoreRequest.builder().id(electricityCabinet.getId().longValue()).tenantId(tenantId)
                        .targetFranchiseeId(targetStore.getFranchiseeId()).targetStoreId(targetStore.getId()).sourceFranchiseeId(assetAllocateRecordRequest.getSourceFranchiseeId())
                        .sourceStoreId(assetAllocateRecordRequest.getSourceStoreId()).sn(electricityCabinet.getSn()).productKey(electricityCabinet.getProductKey())
                        .deviceName(electricityCabinet.getDeviceName()).build()).collect(Collectors.toList());
        
        if (CollectionUtils.isNotEmpty(batchUpdateFranchiseeAndStoreRequestList)) {
            Integer count = electricityCabinetV2Service.batchUpdateFranchiseeIdAndStoreId(batchUpdateFranchiseeAndStoreRequestList);
            
            if (!Objects.equals(count, NumberConstant.ZERO)) {
                // 异步记录
                executorService.execute(() -> {
                    saveAllocateRecords(assetAllocateRecordRequest, null, electricityCabinetList, null, tenantId, uid);
                });
            }
        }
        
        return R.ok();
    }
    
    private R electricityBatteryMove(AssetAllocateRecordRequest assetAllocateRecordRequest, Integer tenantId, List<Long> idList, Long uid) {
        // 获取可调拨的电池
        List<Integer> businessStatusList = List.of(ElectricityBattery.BUSINESS_STATUS_INPUT, ElectricityBattery.BUSINESS_STATUS_RETURN);
        ElectricityBatteryEnableAllocateRequest electricityBatteryEnableAllocateRequest = ElectricityBatteryEnableAllocateRequest.builder().tenantId(tenantId)
                .franchiseeId(assetAllocateRecordRequest.getSourceFranchiseeId()).physicsStatus(ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE).businessStatusList(businessStatusList)
                .idList(idList).build();
        
        List<ElectricityBatteryVO> electricityBatteryList = electricityBatteryService.listEnableAllocateBattery(electricityBatteryEnableAllocateRequest);
        if (CollectionUtils.isEmpty(electricityBatteryList) || !Objects.equals(idList.size(), electricityBatteryList.size())) {
            log.error("ELECTRICITY_BATTERY_MOVE ERROR! has illegal battery! idList={}", idList);
            return R.fail("300812", "您选择的电池编码中存在不可调拨的数据，请刷新页面以获取最新状态后再进行操作");
        }
        
        List<ElectricityBatteryBatchUpdateFranchiseeRequest> batchUpdateFranchiseeRequestList = electricityBatteryList.stream()
                .map(item -> ElectricityBatteryBatchUpdateFranchiseeRequest.builder().sourceFranchiseeId(assetAllocateRecordRequest.getSourceFranchiseeId())
                        .targetFranchiseeId(assetAllocateRecordRequest.getTargetFranchiseeId()).id(item.getId()).sn(item.getSn()).tenantId(tenantId).build())
                .collect(Collectors.toList());
        
        Integer count = electricityBatteryService.batchUpdateFranchiseeId(batchUpdateFranchiseeRequestList);
        
        if (!Objects.equals(count, NumberConstant.ZERO)) {
            // 异步记录
            executorService.execute(() -> {
                saveAllocateRecords(assetAllocateRecordRequest, electricityBatteryList, null, null, tenantId, uid);
            });
        }
        
        return R.ok();
    }
    
    /**
     * 保存资产调拨记录
     */
    public void saveAllocateRecords(AssetAllocateRecordRequest assetAllocateRecordRequest, List<ElectricityBatteryVO> electricityBatteryList,
            List<ElectricityCabinet> electricityCabinetList, List<ElectricityCar> electricityCarList, Integer tenantId, Long uid) {
        String orderNo = OrderIdUtil.generateBusinessOrderId(BusinessType.ASSET_ALLOCATE, uid);
        Long time = System.currentTimeMillis();
        
        AssetAllocateRecordSaveRequest assetAllocateRecordSaveRequest = AssetAllocateRecordSaveRequest.builder().orderNo(orderNo).tenantId(tenantId)
                .oldFranchiseeId(assetAllocateRecordRequest.getSourceFranchiseeId()).newFranchiseeId(assetAllocateRecordRequest.getTargetFranchiseeId())
                .remark(assetAllocateRecordRequest.getRemark()).operator(uid).delFlag(AssetConstant.DEL_NORMAL).createTime(time).updateTime(time).build();
        
        List<AssetAllocateDetailSaveRequest> detailSaveRequestList;
        
        // 封装电池调拨记录
        if (CollectionUtils.isNotEmpty(electricityBatteryList)) {
            assetAllocateRecordSaveRequest.setType(AssetTypeEnum.ASSET_TYPE_BATTERY.getCode());
            
            detailSaveRequestList = electricityBatteryList.stream()
                    .map(item -> AssetAllocateDetailSaveRequest.builder().orderNo(orderNo).tenantId(tenantId).assetId(item.getId()).sn(item.getSn())
                            .modelId(Objects.isNull(item.getModelId()) ? NumberConstant.ZERO_L : item.getModelId()).type(AssetTypeEnum.ASSET_TYPE_BATTERY.getCode())
                            .delFlag(AssetConstant.DEL_NORMAL).createTime(time).updateTime(time).build()).collect(Collectors.toList());
        } else if (CollectionUtils.isNotEmpty(electricityCabinetList)) {
            // 封装电柜调拨记录
            assetAllocateRecordSaveRequest.setType(AssetTypeEnum.ASSET_TYPE_CABINET.getCode());
            assetAllocateRecordSaveRequest.setOldStoreId(assetAllocateRecordRequest.getSourceStoreId());
            assetAllocateRecordSaveRequest.setNewStoreId(assetAllocateRecordRequest.getTargetStoreId());
            
            detailSaveRequestList = electricityCabinetList.stream()
                    .map(item -> AssetAllocateDetailSaveRequest.builder().orderNo(orderNo).tenantId(tenantId).assetId(item.getId().longValue()).sn(item.getSn())
                            .modelId(item.getModelId().longValue()).type(AssetTypeEnum.ASSET_TYPE_CABINET.getCode()).delFlag(AssetConstant.DEL_NORMAL).createTime(time)
                            .updateTime(time).build()).collect(Collectors.toList());
        } else {
            // 封装车辆调拨记录
            assetAllocateRecordSaveRequest.setType(AssetTypeEnum.ASSET_TYPE_CAR.getCode());
            assetAllocateRecordSaveRequest.setOldStoreId(assetAllocateRecordRequest.getSourceStoreId());
            assetAllocateRecordSaveRequest.setNewStoreId(assetAllocateRecordRequest.getTargetStoreId());
            
            detailSaveRequestList = electricityCarList.stream()
                    .map(item -> AssetAllocateDetailSaveRequest.builder().orderNo(orderNo).tenantId(tenantId).assetId(item.getId().longValue()).sn(item.getSn())
                            .modelId(item.getModelId().longValue()).type(AssetTypeEnum.ASSET_TYPE_CAR.getCode()).delFlag(AssetConstant.DEL_NORMAL).createTime(time).updateTime(time)
                            .build()).collect(Collectors.toList());
        }
        
        this.insertOne(assetAllocateRecordSaveRequest);
        
        if (CollectionUtils.isNotEmpty(detailSaveRequestList)) {
            assetAllocateDetailService.batchInsert(detailSaveRequestList);
        }
    }
    
    @Override
    public Integer insertOne(AssetAllocateRecordSaveRequest assetAllocateRecordSaveRequest) {
        AssetAllocateRecordSaveQueryModel assetAllocateRecordSaveQueryModel = new AssetAllocateRecordSaveQueryModel();
        BeanUtils.copyProperties(assetAllocateRecordSaveRequest, assetAllocateRecordSaveQueryModel);
        
        return assetAllocateRecordMapper.insertOne(assetAllocateRecordSaveQueryModel);
    }
    
    @Slave
    @Override
    public List<AssetAllocateRecordVO> listByPage(AssetAllocateRecordPageRequest allocateRecordPageRequest) {
        List<AssetAllocateRecordVO> rspList = null;
        
        AssetAllocateRecordPageQueryModel queryModel = new AssetAllocateRecordPageQueryModel();
        BeanUtil.copyProperties(allocateRecordPageRequest, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()) {
            return new ArrayList<>();
        }
        queryModel.setFranchiseeIds(pair.getRight());
        
        List<AssetAllocateRecordBO> allocateRecordBOList = assetAllocateRecordMapper.selectListByPage(queryModel);
        if (CollectionUtils.isNotEmpty(allocateRecordBOList)) {
            rspList = allocateRecordBOList.stream().map(item -> {
                AssetAllocateRecordVO assetAllocateRecordVO = new AssetAllocateRecordVO();
                BeanUtil.copyProperties(item, assetAllocateRecordVO);
                assetAllocateRecordVO.setSourceFranchiseeId(item.getOldFranchiseeId());
                assetAllocateRecordVO.setTargetFranchiseeId(item.getNewFranchiseeId());
                assetAllocateRecordVO.setSourceStoreId(item.getOldStoreId());
                assetAllocateRecordVO.setTargetStoreId(item.getNewStoreId());
                
                if (Objects.nonNull(item.getOldFranchiseeId())) {
                    assetAllocateRecordVO.setSourceFranchiseeName(
                            Optional.ofNullable(franchiseeService.queryByIdFromCache(item.getOldFranchiseeId())).orElse(new Franchisee()).getName());
                }
                if (Objects.nonNull(item.getNewFranchiseeId())) {
                    assetAllocateRecordVO.setTargetFranchiseeName(
                            Optional.ofNullable(franchiseeService.queryByIdFromCache(item.getNewFranchiseeId())).orElse(new Franchisee()).getName());
                }
                if (Objects.nonNull(item.getOldStoreId())) {
                    assetAllocateRecordVO.setSourceStoreName(Optional.ofNullable(storeService.queryByIdFromCache(item.getOldStoreId())).orElse(new Store()).getName());
                }
                if (Objects.nonNull(item.getNewStoreId())) {
                    assetAllocateRecordVO.setTargetStoreName(Optional.ofNullable(storeService.queryByIdFromCache(item.getNewStoreId())).orElse(new Store()).getName());
                }
                
                List<AssetAllocateDetailVO> allocateDetailVOList = assetAllocateDetailService.listByPage(item.getOrderNo(), TenantContextHolder.getTenantId());
                if (CollectionUtils.isNotEmpty(allocateDetailVOList)) {
                    Set<String> snSet = allocateDetailVOList.stream().map(AssetAllocateDetailVO::getSn).collect(Collectors.toSet());
                    assetAllocateRecordVO.setSnSet(snSet);
                }
                
                return assetAllocateRecordVO;
                
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
        
        return rspList;
    }
    
    @Slave
    @Override
    public Integer countTotal(AssetAllocateRecordPageRequest allocateRecordPageRequest) {
        AssetAllocateRecordPageQueryModel queryModel = new AssetAllocateRecordPageQueryModel();
        BeanUtil.copyProperties(allocateRecordPageRequest, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()) {
            return NumberConstant.ZERO;
        }
        queryModel.setFranchiseeIds(pair.getRight());
        
        return assetAllocateRecordMapper.countTotal(queryModel);
    }
    
    private R judgeInventoryStatus(Long franchiseeId, Integer type) {
        // 查询盘点状态
        Integer inventoryStatus = assetInventoryService.queryInventoryStatusByFranchiseeId(franchiseeId, type);
        if (Objects.equals(inventoryStatus, AssetConstant.ASSET_INVENTORY_STATUS_TAKING)) {
            if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type)) {
                return R.fail("300804", "该加盟商电池资产正在进行盘点，请稍后再试");
            } else if (AssetTypeEnum.ASSET_TYPE_CABINET.getCode().equals(type)) {
                return R.fail("300805", "该加盟商电柜资产正在进行盘点，请稍后再试");
            } else {
                return R.fail("300806", "该加盟商车辆资产正在进行盘点，请稍后再试");
            }
        }
        
        return R.ok();
    }
}
