package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.asset.AssetExitWarehouseBO;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.enums.asset.StockStatusEnum;
import com.xiliulou.electricity.enums.asset.WarehouseOperateTypeEnum;
import com.xiliulou.electricity.mapper.asset.AssetExitWarehouseRecordMapper;
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
import com.xiliulou.electricity.service.asset.AssetExitWarehouseDetailService;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseRecordService;
import com.xiliulou.electricity.service.asset.AssetInventoryService;
import com.xiliulou.electricity.service.asset.AssetWarehouseRecordService;
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
    
    protected XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("ASSET_EXIT_WAREHOUSE_RECORD_HANDLE_THREAD_POOL", 3,
            "asset_exit_warehouse_record_handle_thread_pool");
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private AssetExitWarehouseRecordMapper assetExitWarehouseRecordMapper;
    
    @Autowired
    private ElectricityCabinetV2Service electricityCabinetV2Service;
    
    @Autowired
    private AssetExitWarehouseDetailService assetExitWarehouseDetailService;
    
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
    private AssetWarehouseRecordService assetWarehouseRecordService;
    
    @Resource
    private AssertPermissionService assertPermissionService;
    
    
    @Override
    public R save(AssetExitWarehouseSaveRequest assetExitWarehouseSaveRequest, Long operator) {
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_EXIT_WAREHOUSE_RECORD_LOCK + operator, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            Long franchiseeId = assetExitWarehouseSaveRequest.getFranchiseeId();
            Integer type = assetExitWarehouseSaveRequest.getType();
            Long storeId = assetExitWarehouseSaveRequest.getStoreId();
            List<String> assetList = assetExitWarehouseSaveRequest.getAssetList();
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
    
            if (assetList.size() > AssetConstant.ASSET_EXIT_WAREHOUSE_LIMIT_NUMBER) {
                return R.fail("300813", "资产退库数量最大限制50条，请修改");
            }
    
            Set<Long> idSet = null;
            List<Long> idList;
            List<String> snList;
            List<ElectricityCabinetVO> exitWarehouseCabinetList = null;
            List<ElectricityBatteryVO> exitWarehouseBatteryList = null;
            List<ElectricityCarVO> exitWarehouseCarList = null;
    
            // 盘点状态
            Integer inventoryStatus = assetInventoryService.queryInventoryStatusByFranchiseeId(franchiseeId, type);
            
            if (Objects.equals(AssetConstant.ASSET_EXIT_WAREHOUSE_MODE_ID, assetExitWarehouseSaveRequest.getMode())) {
                // 根据id进行退库
                idSet = assetList.stream().map(Long::parseLong).collect(Collectors.toSet());
            }
            
            // 电柜退库
            if (AssetTypeEnum.ASSET_TYPE_CABINET.getCode().equals(type)) {
                if (Objects.equals(inventoryStatus, AssetConstant.ASSET_INVENTORY_STATUS_TAKING)) {
                    return R.fail("300805", "该加盟商电柜资产正在进行盘点，请稍后再试");
                }
    
                // 根据sn进行退库
                if (Objects.equals(AssetConstant.ASSET_EXIT_WAREHOUSE_MODE_SN, assetExitWarehouseSaveRequest.getMode())) {
                    // 通过sn对柜机进行退库操作，可能会造成同一个sn对多个柜机退库，后期需要优化
                    List<ElectricityCabinetVO> electricityCabinetVOList = electricityCabinetV2Service.listBySnList(assetList, tenantId, franchiseeId);
                    if (CollectionUtils.isEmpty(electricityCabinetVOList)) {
                        log.error("ASSET_EXIT_WAREHOUSE ERROR! electricity not exist, tenantId={}, franchiseeId={}", tenantId, franchiseeId);
                        return R.fail("300814", "上传的电柜编码不存在，请检测后操作");
                    }
                    idSet = electricityCabinetVOList.stream().map(ElectricityCabinetVO::getId).map(Long::valueOf).collect(Collectors.toSet());
                }
    
                // 根据id查询可退库的电柜
                exitWarehouseCabinetList = electricityCabinetV2Service.listEnableExitWarehouseCabinet(idSet, tenantId, franchiseeId, StockStatusEnum.UN_STOCK.getCode());
                if (CollectionUtils.isEmpty(exitWarehouseCabinetList)) {
                    log.error("ASSET_EXIT_WAREHOUSE ERROR! electricity not exist, tenantId={}, franchiseeId={}", tenantId, franchiseeId);
                    return R.fail("300814", "上传的电柜编码不存在，请检测后操作");
                }
    
                idList = exitWarehouseCabinetList.stream().map(ElectricityCabinetVO::getId).map(Long::valueOf).collect(Collectors.toList());
                snList = exitWarehouseCabinetList.stream().map(ElectricityCabinetVO::getSn).collect(Collectors.toList());
            } else if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type)) {
                // 电池退库
                if (Objects.equals(inventoryStatus, AssetConstant.ASSET_INVENTORY_STATUS_TAKING)) {
                    return R.fail("300804", "该加盟商电池资产正在进行盘点，请稍后再试");
                }
    
                // 根据sn进行退库
                if (Objects.equals(AssetConstant.ASSET_EXIT_WAREHOUSE_MODE_SN, assetExitWarehouseSaveRequest.getMode())) {
                    List<ElectricityBattery> electricityBatteryList = electricityBatteryService.listBatteryBySnList(assetList);
                    if (CollectionUtils.isEmpty(electricityBatteryList)) {
                        log.error("ASSET_EXIT_WAREHOUSE ERROR! battery not exist, tenantId={}, franchiseeId={}", tenantId, franchiseeId);
                        return R.fail("300817", "上传的电池编码不存在，请检测后操作");
                    }
                    idSet = electricityBatteryList.stream().map(ElectricityBattery::getId).collect(Collectors.toSet());
                }
    
                // 根据id查询可退库的电池
                exitWarehouseBatteryList = electricityBatteryService.listEnableExitWarehouseBattery(idSet, tenantId, franchiseeId, StockStatusEnum.UN_STOCK.getCode());
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
                
                if (Objects.equals(inventoryStatus, AssetConstant.ASSET_INVENTORY_STATUS_TAKING)) {
                    return R.fail("300806", "该加盟商车辆资产正在进行盘点，请稍后再试");
                }
    
                // 根据sn进行退库
                if (Objects.equals(AssetConstant.ASSET_EXIT_WAREHOUSE_MODE_SN, assetExitWarehouseSaveRequest.getMode())) {
                    List<ElectricityCarVO> electricityCarVOList = electricityCarService.listBySnList(assetList, tenantId, franchiseeId);
                    if (CollectionUtils.isEmpty(electricityCarVOList)) {
                        log.error("ASSET_EXIT_WAREHOUSE ERROR! car not exist, tenantId={}, franchiseeId={}", tenantId, franchiseeId);
                        return R.fail("300818", "上传的车辆编码不存在，请检测后操作");
                    }
                    idSet = electricityCarVOList.stream().map(ElectricityCarVO::getId).collect(Collectors.toSet());
                }
    
                // 根据id查询可退库的车辆
                exitWarehouseCarList = electricityCarService.listEnableExitWarehouseCar(idSet, tenantId, franchiseeId, StockStatusEnum.UN_STOCK.getCode());
                if (CollectionUtils.isEmpty(exitWarehouseCarList)) {
                    log.error("ASSET_EXIT_WAREHOUSE ERROR! car not exist, tenantId={}, franchiseeId={}", tenantId, franchiseeId);
                    return R.fail("300818", "上传的车辆编码不存在，请检测后操作");
                }
    
                idList = exitWarehouseCarList.stream().map(ElectricityCarVO::getId).collect(Collectors.toList());
                snList = exitWarehouseCarList.stream().map(ElectricityCarVO::getSn).collect(Collectors.toList());
            }
            
            Long warehouseId = assetExitWarehouseSaveRequest.getWarehouseId();
            String orderNo = OrderIdUtil.generateBusinessOrderId(BusinessType.ASSET_EXIT_WAREHOUSE, operator);
            Long nowTime = System.currentTimeMillis();
            
            // 封装资产退库记录数据
            AssetExitWarehouseSaveQueryModel recordSaveQueryModel = AssetExitWarehouseSaveQueryModel.builder().orderNo(orderNo).franchiseeId(franchiseeId).storeId(storeId)
                    .type(type).warehouseId(warehouseId).operator(operator).tenantId(tenantId).delFlag(AssetConstant.DEL_NORMAL).createTime(nowTime).updateTime(nowTime)
                    .build();
    
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
            // 持久化
            handleExitWarehouse(assetBatchExitWarehouseRequest, type, recordSaveQueryModel, detailSaveQueryModelList, operator, snList);
            // 清理缓存
            handleClearCache(exitWarehouseCabinetList, exitWarehouseBatteryList, exitWarehouseCarList);
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_EXIT_WAREHOUSE_RECORD_LOCK + operator);
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void handleExitWarehouse(AssetBatchExitWarehouseRequest assetBatchExitWarehouseRequest, Integer type, AssetExitWarehouseSaveQueryModel recordSaveQueryModel,
            List<AssetExitWarehouseDetailSaveQueryModel> detailSaveQueryModelList, Long operator, List<String> snList) {
        Integer count;
        if (AssetTypeEnum.ASSET_TYPE_CABINET.getCode().equals(type)) {
            // 电柜批量退库
            count = electricityCabinetV2Service.batchExitWarehouse(assetBatchExitWarehouseRequest);
        } else if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type)) {
            // 电池批量退库
            count = electricityBatteryService.batchExitWarehouse(assetBatchExitWarehouseRequest);
        } else {
            //车辆批量退库
            count = electricityCarService.batchExitWarehouse(assetBatchExitWarehouseRequest);
        }
        
        //异步记录
        if (!Objects.equals(count, NumberConstant.ZERO)) {
            executorService.execute(() -> {
                // 新增资产退库记录
                insertOne(recordSaveQueryModel);
                // 新增资产退库详情
                assetExitWarehouseDetailService.batchInsert(detailSaveQueryModelList, operator);
                
                //库房记录
                Long warehouseId = assetBatchExitWarehouseRequest.getWarehouseId();
                if (Objects.nonNull(warehouseId) && !Objects.equals(warehouseId, NumberConstant.ZERO_L)) {
                    
                    assetWarehouseRecordService.asyncRecordByWarehouseId(assetBatchExitWarehouseRequest.getTenantId(), operator, warehouseId, snList, type,
                            WarehouseOperateTypeEnum.WAREHOUSE_OPERATE_TYPE_EXIT.getCode());
                }
            });
        }
    }
    
    private void handleClearCache(List<ElectricityCabinetVO> electricityCabinetVOList, List<ElectricityBatteryVO> electricityBatteryVOList,
            List<ElectricityCarVO> electricityCarVOList) {
        //清理柜机缓存
        if (CollectionUtils.isNotEmpty(electricityCabinetVOList)) {
            electricityCabinetVOList.forEach(electricityCabinet -> {
                redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId());
                redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
            });
        }
        
        //清理电池缓存
        if (CollectionUtils.isNotEmpty(electricityBatteryVOList)) {
            electricityBatteryVOList.forEach(electricityBattery -> {
                redisService.delete(CacheConstant.CACHE_BT_ATTR + electricityBattery.getSn());
            });
        }
        
        //清理车辆缓存
        if (CollectionUtils.isNotEmpty(electricityCarVOList)) {
            if (CollectionUtils.isNotEmpty(electricityCarVOList)) {
                electricityCarVOList.forEach(electricityCar -> {
                    redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + electricityCar.getId());
                });
            }
        }
    }
    
    @Override
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
        if (!pair.getLeft()){
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
        if (!pair.getLeft()){
            return NumberConstant.ZERO;
        }
        assetExitWarehouseQueryModel.setFranchiseeIds(pair.getRight());
        
        return assetExitWarehouseRecordMapper.countTotal(assetExitWarehouseQueryModel);
    }
    
    
    
}
