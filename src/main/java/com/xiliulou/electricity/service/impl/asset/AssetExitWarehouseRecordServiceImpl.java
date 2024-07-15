package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.asset.AssetBatchExitWarehouseBO;
import com.xiliulou.electricity.bo.asset.AssetExitWarehouseBO;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.enums.asset.StockStatusEnum;
import com.xiliulou.electricity.mapper.asset.AssetExitWarehouseRecordMapper;
import com.xiliulou.electricity.query.asset.AssetEnableExitWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.AssetExitWarehouseDetailSaveQueryModel;
import com.xiliulou.electricity.query.asset.AssetExitWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.AssetExitWarehouseSaveQueryModel;
import com.xiliulou.electricity.request.asset.AssetBatchExitWarehouseRequest;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseRecordRequest;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseSaveRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseRecordService;
import com.xiliulou.electricity.service.asset.AssetInventoryService;
import com.xiliulou.electricity.service.asset.AssetManageService;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.ElectricityCarVO;
import com.xiliulou.electricity.vo.asset.AssetExitWarehouseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 退库业务
 * @date 2023/11/27 09:36:23
 */
@Slf4j
@Service
public class AssetExitWarehouseRecordServiceImpl implements AssetExitWarehouseRecordService {
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private AssetExitWarehouseRecordMapper assetExitWarehouseRecordMapper;
    
    @Autowired
    private ElectricityCabinetV2Service electricityCabinetV2Service;
    
    @Autowired
    private ElectricityCarService electricityCarService;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Autowired
    private StoreService storeService;
    
    @Autowired
    private AssetInventoryService assetInventoryService;
    
    @Autowired
    private ElectricityBatteryService electricityBatteryService;
    
    @Resource
    private AssertPermissionService assertPermissionService;
    
    @Resource
    private AssetManageService assetManageService;
    
    
    @Override
    public R save(AssetExitWarehouseSaveRequest assetExitWarehouseSaveRequest, Long operator) {
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_EXIT_WAREHOUSE_RECORD_LOCK + operator, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            Long franchiseeId = assetExitWarehouseSaveRequest.getFranchiseeId();
            List<String> assetList = assetExitWarehouseSaveRequest.getAssetList();
            Integer type = assetExitWarehouseSaveRequest.getType();
            Integer tenantId = TenantContextHolder.getTenantId();
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
            if (Objects.isNull(franchisee)) {
                log.error("ASSET_EXIT_WAREHOUSE ERROR! not found franchise! franchiseId={}", assetExitWarehouseSaveRequest.getFranchiseeId());
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
            
            // tenantId校验
            if (!Objects.equals(franchisee.getTenantId(), tenantId)) {
                return R.ok();
            }
            
            // 盘点盘点状态
            R inventoryStatus = judgeInventoryStatus(franchiseeId, type);
            if (!inventoryStatus.isSuccess()) {
                return R.fail(inventoryStatus.getErrCode(), inventoryStatus.getErrMsg());
            }
            
            if (assetList.size() > AssetConstant.ASSET_EXIT_WAREHOUSE_LIMIT_NUMBER) {
                return R.fail("300813", "资产退库数量最大限制3000条，请修改");
            }
            
            List<List<String>> partition = ListUtils.partition(assetList, 1000);
            List<AssetBatchExitWarehouseBO> dataList = new ArrayList<>(partition.size());
            String orderNo = OrderIdUtil.generateBusinessOrderId(BusinessType.ASSET_EXIT_WAREHOUSE, operator);
            for (List<String> list : partition) {
                R r = assembleData(assetExitWarehouseSaveRequest, tenantId, franchiseeId, operator, list, type, orderNo);
                if (!r.isSuccess()) {
                    return R.fail(r.getErrCode(), r.getErrMsg());
                } else {
                    dataList.add((AssetBatchExitWarehouseBO) r.getData());
                }
            }
            
            List<String> notSameFranchiseeSnList = null;
            
            // 处理数据
            if (CollectionUtils.isNotEmpty(dataList)) {
                // 持久化
                assetManageService.batchExistWarehouseTx(dataList);
                
                // 清除缓存
                handleClearCache(dataList);
                
                // 返回加盟商不一致的sn
                notSameFranchiseeSnList = handleNotSameFranchiseeSn(dataList);
            }
            
            if (CollectionUtils.isEmpty(notSameFranchiseeSnList)) {
                return R.ok(Collections.emptyList());
            }
            
            return R.ok(notSameFranchiseeSnList);
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_EXIT_WAREHOUSE_RECORD_LOCK + operator);
        }
    }
    
    private List<String> handleNotSameFranchiseeSn(List<AssetBatchExitWarehouseBO> dataList) {
        List<String> list = new ArrayList<>();
        dataList.forEach(data -> {
            List<String> notSameFranchiseeSnList = data.getNotSameFranchiseeSnList();
            if (CollectionUtils.isNotEmpty(notSameFranchiseeSnList)) {
                list.addAll(notSameFranchiseeSnList);
            }
        });
        
        return list;
    }
    
    private R assembleData(AssetExitWarehouseSaveRequest assetExitWarehouseSaveRequest, Integer tenantId, Long franchiseeId, Long operator, List<String> assetList, Integer type,
            String orderNo) {
        Long storeId = assetExitWarehouseSaveRequest.getStoreId();
        List<Long> idList;
        List<String> snList;
        List<ElectricityCabinetVO> exitWarehouseCabinetList = null;
        List<ElectricityBatteryVO> exitWarehouseBatteryList = null;
        List<ElectricityCarVO> exitWarehouseCarList = null;
        
        // 封装查询条件
        AssetEnableExitWarehouseQueryModel queryModel = AssetEnableExitWarehouseQueryModel.builder().tenantId(tenantId).franchiseeId(franchiseeId)
                .stockStatus(StockStatusEnum.UN_STOCK.getCode()).build();
        
        boolean snFlag = false;
        List<String> notSameFranchiseeSnList = null;
        if (Objects.equals(AssetConstant.ASSET_EXIT_WAREHOUSE_MODE_ID, assetExitWarehouseSaveRequest.getMode())) {
            // 根据id进行退库
            Set<Long> idSet = assetList.stream().map(Long::parseLong).collect(Collectors.toSet());
            queryModel.setIdSet(idSet);
        } else {
            // 根据sn退库
            snFlag = true;
            queryModel.setSnList(assetList);
        }
        
        // 电柜退库
        if (AssetTypeEnum.ASSET_TYPE_CABINET.getCode().equals(type)) {
            // 查询可退库的电柜
            exitWarehouseCabinetList = electricityCabinetV2Service.listEnableExitWarehouseCabinet(queryModel);
            if (CollectionUtils.isEmpty(exitWarehouseCabinetList)) {
                log.error("ASSET_EXIT_WAREHOUSE ERROR! electricity not exist, tenantId={}, franchiseeId={}", tenantId, franchiseeId);
                return R.fail("300814", "上传的电柜编码不存在，请检测后操作");
            }
            
            idList = exitWarehouseCabinetList.stream().map(ElectricityCabinetVO::getId).map(Long::valueOf).collect(Collectors.toList());
            snList = exitWarehouseCabinetList.stream().map(ElectricityCabinetVO::getSn).collect(Collectors.toList());
        } else if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type)) {
            // 查询可退库的电池
            exitWarehouseBatteryList = electricityBatteryService.listEnableExitWarehouseBattery(queryModel);
            if (CollectionUtils.isEmpty(exitWarehouseBatteryList)) {
                log.error("ASSET_EXIT_WAREHOUSE ERROR! battery not exist, tenantId={}, franchiseeId={}", tenantId, franchiseeId);
                return R.fail("300817", "上传的电池编码不存在，请检测后操作");
            }
            
            idList = exitWarehouseBatteryList.stream().map(ElectricityBatteryVO::getId).collect(Collectors.toList());
            snList = exitWarehouseBatteryList.stream().map(ElectricityBatteryVO::getSn).collect(Collectors.toList());
        } else {
            // 车辆退库
            if (Objects.isNull(storeId)) {
                return R.fail("300807", "退库门店不存在，请刷新页面以获取最新状态后再进行操作");
            }
            
            Store store = storeService.queryByIdFromCache(storeId);
            if (Objects.isNull(store)) {
                log.error("ASSET_EXIT_WAREHOUSE ERROR! not found store! store={}", assetExitWarehouseSaveRequest.getStoreId());
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }
            
            if (!Objects.equals(store.getTenantId(), TenantContextHolder.getTenantId())) {
                return R.ok();
            }
            
            // 查询可退库的车辆
            exitWarehouseCarList = electricityCarService.listEnableExitWarehouseCar(queryModel);
            if (CollectionUtils.isEmpty(exitWarehouseCarList)) {
                log.error("ASSET_EXIT_WAREHOUSE ERROR! car not exist, tenantId={}, franchiseeId={}", tenantId, franchiseeId);
                return R.fail("300818", "上传的车辆编码不存在，请检测后操作");
            }
            
            idList = exitWarehouseCarList.stream().map(ElectricityCarVO::getId).collect(Collectors.toList());
            snList = exitWarehouseCarList.stream().map(ElectricityCarVO::getSn).collect(Collectors.toList());
        }
        
        // 根据sn退库时，返回加盟商不一致的sn
        if (snFlag && !Objects.equals(snList.size(), queryModel.getSnList().size())) {
            List<String> querySnList = queryModel.getSnList();
            
            notSameFranchiseeSnList = querySnList.stream().filter(sn -> !snList.contains(sn)).collect(Collectors.toList());
        }
        
        Long warehouseId = assetExitWarehouseSaveRequest.getWarehouseId();
        Long nowTime = System.currentTimeMillis();
        
        // 封装资产退库记录数据
        AssetExitWarehouseSaveQueryModel recordSaveQueryModel = AssetExitWarehouseSaveQueryModel.builder().orderNo(orderNo).franchiseeId(franchiseeId).storeId(storeId).type(type)
                .warehouseId(warehouseId).operator(operator).tenantId(tenantId).delFlag(AssetConstant.DEL_NORMAL).createTime(nowTime).updateTime(nowTime).build();
        
        // 封装资产退库详情数据
        AssetExitWarehouseDetailSaveQueryModel detailSaveQueryModel = AssetExitWarehouseDetailSaveQueryModel.builder().orderNo(orderNo).type(type).tenantId(tenantId)
                .delFlag(AssetConstant.DEL_NORMAL).createTime(nowTime).updateTime(nowTime).build();
        
        List<AssetExitWarehouseDetailSaveQueryModel> detailSaveQueryModelList = snList.stream().map(sn -> {
            AssetExitWarehouseDetailSaveQueryModel assetExitWarehouseDetailSaveQueryModel = new AssetExitWarehouseDetailSaveQueryModel();
            BeanUtils.copyProperties(detailSaveQueryModel, assetExitWarehouseDetailSaveQueryModel);
            assetExitWarehouseDetailSaveQueryModel.setSn(sn);
            
            return assetExitWarehouseDetailSaveQueryModel;
            
        }).collect(Collectors.toList());
        
        // 封装 电池/电柜/车辆 库存状态修改的请求
        AssetBatchExitWarehouseRequest assetBatchExitWarehouseRequest = AssetBatchExitWarehouseRequest.builder().tenantId(tenantId).franchiseeId(franchiseeId)
                .warehouseId(warehouseId).idList(idList).build();
        
        AssetBatchExitWarehouseBO data = AssetBatchExitWarehouseBO.builder().assetBatchExitWarehouseRequest(assetBatchExitWarehouseRequest)
                .recordSaveQueryModel(recordSaveQueryModel).detailSaveQueryModelList(detailSaveQueryModelList).snList(snList).type(type).operator(operator)
                .exitWarehouseCabinetList(exitWarehouseCabinetList).exitWarehouseBatteryList(exitWarehouseBatteryList).exitWarehouseCarList(exitWarehouseCarList)
                .notSameFranchiseeSnList(notSameFranchiseeSnList).build();
        
        return R.ok(data);
    }
    
    private R judgeInventoryStatus(Long franchiseeId, Integer type) {
        // 查询盘点状态
        Integer inventoryStatus = assetInventoryService.queryInventoryStatusByFranchiseeId(franchiseeId, type);
        if (AssetTypeEnum.ASSET_TYPE_CABINET.getCode().equals(type)) {
            if (Objects.equals(inventoryStatus, AssetConstant.ASSET_INVENTORY_STATUS_TAKING)) {
                return R.fail("300805", "该加盟商电柜资产正在进行盘点，请稍后再试");
            }
        } else if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type)) {
            // 电池退库
            if (Objects.equals(inventoryStatus, AssetConstant.ASSET_INVENTORY_STATUS_TAKING)) {
                return R.fail("300804", "该加盟商电池资产正在进行盘点，请稍后再试");
            }
        } else {
            if (Objects.equals(inventoryStatus, AssetConstant.ASSET_INVENTORY_STATUS_TAKING)) {
                return R.fail("300806", "该加盟商车辆资产正在进行盘点，请稍后再试");
            }
        }
        
        return R.ok();
    }
    
    private void handleClearCache(List<AssetBatchExitWarehouseBO> dataList) {
        dataList.forEach(data -> {
            // 电池(key bt_attr:)无需清除缓存
            // 清除柜机缓存
            if (CollectionUtils.isNotEmpty(data.getExitWarehouseCabinetList())) {
                List<ElectricityCabinetVO> cabinetList = data.getExitWarehouseCabinetList();
                if (CollectionUtils.isEmpty(cabinetList)) {
                    return;
                }
                
                cabinetList.parallelStream().forEach(cabinet -> {
                    redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + cabinet.getId());
                    redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + cabinet.getProductKey() + cabinet.getDeviceName());
                });
            }
            
            // 清除车辆缓存
            if (CollectionUtils.isNotEmpty(data.getExitWarehouseCarList())) {
                List<ElectricityCarVO> carList = data.getExitWarehouseCarList();
                if (CollectionUtils.isEmpty(carList)) {
                    return;
                }
                
                carList.parallelStream().forEach(car -> {
                    redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + car.getId());
                });
            }
        });
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insertOne(AssetExitWarehouseSaveQueryModel recordSaveQueryModel) {
        return assetExitWarehouseRecordMapper.insertOne(recordSaveQueryModel);
    }
    
    @Slave
    @Override
    public List<AssetExitWarehouseVO> listByPage(AssetExitWarehouseRecordRequest assetExitWarehouseRecordRequest) {
        AssetExitWarehouseQueryModel assetExitWarehouseQueryModel = new AssetExitWarehouseQueryModel();
        BeanUtils.copyProperties(assetExitWarehouseRecordRequest, assetExitWarehouseQueryModel);
        assetExitWarehouseQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()) {
            return new ArrayList<>();
        }
        assetExitWarehouseQueryModel.setFranchiseeIds(pair.getRight());
        
        List<AssetExitWarehouseVO> rspList = Collections.emptyList();
        List<AssetExitWarehouseBO> assetExitWarehouseBOList = assetExitWarehouseRecordMapper.selectListByFranchiseeId(assetExitWarehouseQueryModel);
        if (CollectionUtils.isNotEmpty(assetExitWarehouseBOList)) {
            rspList = assetExitWarehouseBOList.stream().map(item -> {
                
                AssetExitWarehouseVO assetExitWarehouseVO = new AssetExitWarehouseVO();
                BeanUtils.copyProperties(item, assetExitWarehouseVO);
                if (Objects.nonNull(item.getFranchiseeId())) {
                    assetExitWarehouseVO.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(item.getFranchiseeId())).orElse(new Franchisee()).getName());
                }
                
                if (Objects.nonNull(item.getStoreId())) {
                    assetExitWarehouseVO.setStoreName(Optional.ofNullable(storeService.queryByIdFromCache(item.getStoreId())).orElse(new Store()).getName());
                }
                
                return assetExitWarehouseVO;
                
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
        
        return rspList;
    }
    
    @Slave
    @Override
    public Integer countTotal(AssetExitWarehouseRecordRequest assetExitWarehouseRecordRequest) {
        AssetExitWarehouseQueryModel assetExitWarehouseQueryModel = new AssetExitWarehouseQueryModel();
        BeanUtils.copyProperties(assetExitWarehouseRecordRequest, assetExitWarehouseQueryModel);
        assetExitWarehouseQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()) {
            return NumberConstant.ZERO;
        }
        assetExitWarehouseQueryModel.setFranchiseeIds(pair.getRight());
        
        return assetExitWarehouseRecordMapper.countTotal(assetExitWarehouseQueryModel);
    }
    
    
}
