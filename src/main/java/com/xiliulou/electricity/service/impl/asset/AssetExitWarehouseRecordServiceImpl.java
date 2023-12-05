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
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.enums.asset.StockStatusEnum;
import com.xiliulou.electricity.mapper.asset.AssetExitWarehouseRecordMapper;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseDetailSaveQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseSaveQueryModel;
import com.xiliulou.electricity.request.asset.AssetBatchExitWarehouseBySnRequest;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseRecordRequest;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseSaveRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseDetailService;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseRecordService;
import com.xiliulou.electricity.service.asset.AssetInventoryService;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.ElectricityCarVO;
import com.xiliulou.electricity.vo.asset.AssetExitWarehouseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    @Autowired
    private ElectricityCabinetService electricityCabinetService;
    
    
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
        
            Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
            if (Objects.isNull(franchisee)) {
                log.error("ASSET_EXIT_WAREHOUSE ERROR! not found franchise! franchiseId={}", assetExitWarehouseSaveRequest.getFranchiseeId());
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
        
            if (!Objects.equals(franchisee.getTenantId(), TenantContextHolder.getTenantId())) {
                return R.ok();
            }
        
            // 车辆退库类型门店必填
            if (AssetTypeEnum.ASSET_TYPE_CAR.getCode().equals(type)) {
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
            }
        
            // 校验加盟商是否正在进行资产盘点
            Integer inventoryStatus = assetInventoryService.queryInventoryStatusByFranchiseeId(franchiseeId, type);
            if (Objects.equals(inventoryStatus, AssetConstant.ASSET_INVENTORY_STATUS_TAKING)) {
                if (AssetTypeEnum.ASSET_TYPE_CABINET.getCode().equals(type)) {
                    return R.fail("300805", "该加盟商电柜资产正在进行盘点，请稍后再试");
                } else if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type)) {
                    return R.fail("300804", "该加盟商电池资产正在进行盘点，请稍后再试");
                } else {
                    return R.fail("300806", "该加盟商车辆资产正在进行盘点，请稍后再试");
                }
            }
        
            if (CollectionUtils.isNotEmpty(assetExitWarehouseSaveRequest.getAssetList())) {
                List<String> assetList = assetExitWarehouseSaveRequest.getAssetList();
            
                if (CollectionUtils.isNotEmpty(assetList) && assetList.size() > AssetConstant.ASSET_EXIT_WAREHOUSE_LIMIT_NUMBER) {
                    return R.fail("300813", "资产退库数量最大限制50条，请修改");
                }
                
                //如果mode=1,assetList封装的是id，根据id查询sn
                if (Objects.equals(NumberConstant.ONE, assetExitWarehouseSaveRequest.getMode())) {
                    handleListByIds(assetList, type);
                }
            
                // 对snList进行有效性校验
                handleInvalidSnList(assetList, type);
                if (CollectionUtils.isEmpty(assetList)) {
                    return R.fail("300814", "上传的车辆/电池/电柜编码不存在，请检测后操作");
                }
            
                Integer tenantId = TenantContextHolder.getTenantId();
                Long warehouseId = assetExitWarehouseSaveRequest.getWarehouseId();
                String orderNo = OrderIdUtil.generateBusinessOrderId(BusinessType.ASSET_EXIT_WAREHOUSE, operator);
                Long nowTime = System.currentTimeMillis();
            
                // 封装资产退库记录数据
                AssetExitWarehouseSaveQueryModel assetExitWarehouseSaveQueryModel = AssetExitWarehouseSaveQueryModel.builder().orderNo(orderNo).franchiseeId(franchiseeId)
                        .storeId(storeId).type(type).warehouseId(warehouseId).operator(operator).tenantId(tenantId).delFlag(AssetConstant.DEL_NORMAL).createTime(nowTime)
                        .updateTime(nowTime).build();
            
                // 封装资产退库详情数据
                AssetExitWarehouseDetailSaveQueryModel detailSaveQueryModel = AssetExitWarehouseDetailSaveQueryModel.builder().orderNo(orderNo).type(type).tenantId(tenantId)
                        .delFlag(AssetConstant.DEL_NORMAL).createTime(nowTime).updateTime(nowTime).build();
            
                List<AssetExitWarehouseDetailSaveQueryModel> detailSaveQueryModelList;
                detailSaveQueryModelList = assetList.stream().map(sn -> {
                    AssetExitWarehouseDetailSaveQueryModel assetExitWarehouseDetailSaveQueryModel = new AssetExitWarehouseDetailSaveQueryModel();
                    BeanUtils.copyProperties(detailSaveQueryModel, assetExitWarehouseDetailSaveQueryModel);
                    assetExitWarehouseDetailSaveQueryModel.setSn(sn);
                
                    return assetExitWarehouseDetailSaveQueryModel;
                
                }).collect(Collectors.toList());
            
                // 根据sn封装 电池/电柜/车辆 的库存状态数据
                AssetBatchExitWarehouseBySnRequest assetBatchExitWarehouseBySnRequest = AssetBatchExitWarehouseBySnRequest.builder().tenantId(tenantId).franchiseeId(franchiseeId)
                        .warehouseId(warehouseId).snList(assetList).build();
            
                // 持久化
                handleExitWarehouse(assetExitWarehouseSaveQueryModel, detailSaveQueryModelList, assetBatchExitWarehouseBySnRequest, operator, type);
            
                // 清理缓存
                handleClearCache(assetList, type, tenantId, franchiseeId);
            }
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_EXIT_WAREHOUSE_RECORD_LOCK + operator);
        }
    }
    
    private List<String> handleListByIds(List<String> assetList, Integer type) {
        List<String> snList = null;
        Set<Integer> idIntegerSet = assetList.stream().map(Integer::parseInt).collect(Collectors.toSet());
        Set<Long> idLongSet = assetList.stream().map(Long::parseLong).collect(Collectors.toSet());
        
        if (AssetTypeEnum.ASSET_TYPE_CABINET.getCode().equals(type)) {
            List<ElectricityCabinet> electricityCabinetList = electricityCabinetService.listByIds(idIntegerSet);
            if (CollectionUtils.isNotEmpty(electricityCabinetList)){
                snList = electricityCabinetList.stream().map(ElectricityCabinet::getSn).collect(Collectors.toList());
            }
        } else if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type)) {
            List<ElectricityBattery> electricityBatteryList = electricityBatteryService.selectByBatteryIds(new ArrayList<>(idLongSet));
            if (CollectionUtils.isNotEmpty(electricityBatteryList)){
                snList = electricityBatteryList.stream().map(ElectricityBattery::getSn).collect(Collectors.toList());
            }
        } else {
            List<ElectricityCarVO> electricityCarVOList = electricityCarService.listByIds(idLongSet);
            if (CollectionUtils.isNotEmpty(electricityCarVOList)){
                snList = electricityCarVOList.stream().map(ElectricityCarVO::getSn).collect(Collectors.toList());
            }
        }
        
        return snList;
    }
    
    private void handleInvalidSnList(List<String> snList, Integer type) {
        if (CollectionUtils.isNotEmpty(snList)) {
            // 过滤掉不存在的及已出库的
            List<String> removeList = new ArrayList<>();
            if (AssetTypeEnum.ASSET_TYPE_CABINET.getCode().equals(type)) {
                snList.forEach(sn -> {
                    List<ElectricityCabinetVO> electricityCabinetVOList = electricityCabinetV2Service.queryEnableExitWarehouseBySn(sn, TenantContextHolder.getTenantId(),
                            StockStatusEnum.UN_STOCK.getCode());
                    if (CollectionUtils.isEmpty(electricityCabinetVOList)) {
                        removeList.add(sn);
                    }
                });
            } else if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type)) {
                snList.forEach(sn -> {
                    ElectricityBatteryVO electricityBatteryVO = electricityBatteryService.queryEnableExitWarehouseBySn(sn, TenantContextHolder.getTenantId(),
                            StockStatusEnum.UN_STOCK.getCode());
                    if (Objects.isNull(electricityBatteryVO)) {
                        removeList.add(sn);
                    }
                });
            } else {
                snList.forEach(sn -> {
                    ElectricityCarVO electricityCarVO = electricityCarService.queryEnableExitWarehouseBySn(sn, TenantContextHolder.getTenantId(),
                            StockStatusEnum.UN_STOCK.getCode());
                    if (Objects.isNull(electricityCarVO)) {
                        removeList.add(sn);
                    }
                });
            }
            snList.removeAll(removeList);
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void handleExitWarehouse(AssetExitWarehouseSaveQueryModel exitWarehouseSaveQueryModel, List<AssetExitWarehouseDetailSaveQueryModel> detailSaveQueryModelList,
            AssetBatchExitWarehouseBySnRequest assetBatchExitWarehouseBySnRequest, Long operator, Integer type) {
        R result;
        if (AssetTypeEnum.ASSET_TYPE_CABINET.getCode().equals(type)) {
            // 电柜批量退库
            result = electricityCabinetV2Service.batchExitWarehouseBySn(assetBatchExitWarehouseBySnRequest);
        } else if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type)) {
            // 电池批量退库
            result = electricityBatteryService.batchExitWarehouseBySn(assetBatchExitWarehouseBySnRequest);
        } else {
            //车辆批量退库
            result = electricityCarService.batchExitWarehouseBySn(assetBatchExitWarehouseBySnRequest);
        }
        
        //异步记录
        if (Objects.nonNull(result) && (Integer) result.getData() > NumberConstant.ZERO) {
            executorService.execute(() -> {
                // 新增资产退库记录
                assetExitWarehouseRecordMapper.insertOne(exitWarehouseSaveQueryModel);
                // 新增资产退库详情
                assetExitWarehouseDetailService.batchInsert(detailSaveQueryModelList, operator);
            });
        }
    }
    
    private void handleClearCache(List<String> snList, Integer type, Integer tenantId, Long franchiseeId) {
        //清理柜机缓存
        if (AssetTypeEnum.ASSET_TYPE_CABINET.getCode().equals(type)) {
            List<ElectricityCabinetVO> electricityCabinetVOList = electricityCabinetV2Service.listBySnList(snList, tenantId, franchiseeId);
            if (CollectionUtils.isNotEmpty(electricityCabinetVOList)) {
                electricityCabinetVOList.forEach(electricityCabinet -> {
                    redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId());
                    redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
                });
            }
        } else if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type)) {
            //清理电池缓存
            snList.forEach(sn -> {
                redisService.delete(CacheConstant.CACHE_BT_ATTR + sn);
            });
        } else {
            //清理车辆缓存
            List<ElectricityCarVO> electricityCarVOList = electricityCarService.listBySnList(snList, tenantId, franchiseeId);
            if (CollectionUtils.isNotEmpty(electricityCarVOList)) {
                electricityCarVOList.forEach(electricityCar -> {
                    redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + electricityCar.getId());
                });
            }
        }
    }
    
    @Slave
    @Override
    public List<AssetExitWarehouseVO> listByPage(AssetExitWarehouseRecordRequest assetExitWarehouseRecordRequest) {
        AssetExitWarehouseQueryModel assetExitWarehouseQueryModel = new AssetExitWarehouseQueryModel();
        BeanUtils.copyProperties(assetExitWarehouseRecordRequest, assetExitWarehouseQueryModel);
        assetExitWarehouseQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        List<AssetExitWarehouseVO> rspList = Collections.emptyList();
        
        List<AssetExitWarehouseBO> assetExitWarehouseBOList = assetExitWarehouseRecordMapper.selectListByFranchiseeId(assetExitWarehouseQueryModel);
        if (CollectionUtils.isNotEmpty(assetExitWarehouseBOList)) {
            rspList = assetExitWarehouseBOList.stream().map(item -> {
                
                Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
                Store store = storeService.queryByIdFromCache(item.getStoreId());
                
                AssetExitWarehouseVO assetExitWarehouseVO = new AssetExitWarehouseVO();
                BeanUtils.copyProperties(item, assetExitWarehouseVO);
                assetExitWarehouseVO.setFranchiseeName(franchisee.getName());
                if (Objects.nonNull(store)) {
                    assetExitWarehouseVO.setStoreName(store.getName());
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
        
        return assetExitWarehouseRecordMapper.countTotal(assetExitWarehouseQueryModel);
    }
}
