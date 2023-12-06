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
import com.xiliulou.electricity.mapper.asset.AssetExitWarehouseRecordMapper;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseDetailSaveQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseSaveQueryModel;
import com.xiliulou.electricity.request.asset.AssetBatchExitWarehouseRequest;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseRecordRequest;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseSaveRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
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
            
            Integer tenantId = TenantContextHolder.getTenantId();
            Long warehouseId = assetExitWarehouseSaveRequest.getWarehouseId();
            String orderNo = OrderIdUtil.generateBusinessOrderId(BusinessType.ASSET_EXIT_WAREHOUSE, operator);
            Long nowTime = System.currentTimeMillis();
            
            if (CollectionUtils.isNotEmpty(assetExitWarehouseSaveRequest.getAssetList())) {
                List<String> assetList = assetExitWarehouseSaveRequest.getAssetList();
                
                if (CollectionUtils.isNotEmpty(assetList) && assetList.size() > AssetConstant.ASSET_EXIT_WAREHOUSE_LIMIT_NUMBER) {
                    return R.fail("300813", "资产退库数量最大限制50条，请修改");
                }
                
                Set<Integer> idIntegerSet = null;
                Set<Long> idLongSet = null;
                //根据id退库
                if (Objects.equals(NumberConstant.ONE, assetExitWarehouseSaveRequest.getMode())) {
                    idIntegerSet = assetList.stream().map(Integer::parseInt).collect(Collectors.toSet());
                    idLongSet = assetList.stream().map(Long::parseLong).collect(Collectors.toSet());
                } else {
                    //根据sn退库，通过sn获取id进行退库
                    if (AssetTypeEnum.ASSET_TYPE_CABINET.getCode().equals(type)) {
                        List<ElectricityCabinetVO> electricityCabinetVOList = electricityCabinetV2Service.listBySnList(assetList, tenantId, franchiseeId);
                        if (CollectionUtils.isEmpty(electricityCabinetVOList)) {
                            return R.fail("300814", "上传的电柜编码不存在，请检测后操作");
                        }
                        idIntegerSet = electricityCabinetVOList.stream().map(ElectricityCabinetVO::getId).collect(Collectors.toSet());
                        
                    } else if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type)) {
                        List<ElectricityBattery> electricityBatteryList = electricityBatteryService.listBatteryBySnList(assetList);
                        if (CollectionUtils.isEmpty(electricityBatteryList)) {
                            return R.fail("300817", "上传的电池编码不存在，请检测后操作");
                        }
                        idLongSet = electricityBatteryList.stream().map(ElectricityBattery::getId).collect(Collectors.toSet());
                    } else {
                        List<ElectricityCarVO> electricityCarVOList = electricityCarService.listBySnList(assetList, tenantId, franchiseeId);
                        if (CollectionUtils.isEmpty(electricityCarVOList)) {
                            return R.fail("300818", "上传的车辆编码不存在，请检测后操作");
                        }
                        idLongSet = electricityCarVOList.stream().map(ElectricityCarVO::getId).collect(Collectors.toSet());
                    }
                }
                
                // 根据id进行退库
                List<ElectricityCabinetVO> electricityCabinetVOList = null;
                List<ElectricityBatteryVO> electricityBatteryVOList = null;
                List<ElectricityCarVO> electricityCarVOList = null;
                List<Integer> idIntegerList = null;
                List<Long> idLongList = null;
                List<String> snList;
                
                Integer inventoryStatus = assetInventoryService.queryInventoryStatusByFranchiseeId(franchiseeId, type);
                if (AssetTypeEnum.ASSET_TYPE_CABINET.getCode().equals(type)) {
                    if (Objects.equals(inventoryStatus, AssetConstant.ASSET_INVENTORY_STATUS_TAKING)) {
                        return R.fail("300805", "该加盟商电柜资产正在进行盘点，请稍后再试");
                    }
                    // 根据id查询可退库的电柜
                    electricityCabinetVOList = electricityCabinetV2Service.listEnableExitWarehouseCabinet(idIntegerSet, tenantId, StockStatusEnum.UN_STOCK.getCode());
                    if (CollectionUtils.isEmpty(electricityCabinetVOList)) {
                        return R.fail("300814", "上传的电柜编码不存在，请检测后操作");
                    }
                    
                    idIntegerList = electricityCabinetVOList.stream().map(ElectricityCabinetVO::getId).collect(Collectors.toList());
                    snList = electricityCabinetVOList.stream().map(ElectricityCabinetVO::getSn).collect(Collectors.toList());
                    
                } else if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type)) {
                    if (Objects.equals(inventoryStatus, AssetConstant.ASSET_INVENTORY_STATUS_TAKING)) {
                        return R.fail("300804", "该加盟商电池资产正在进行盘点，请稍后再试");
                    }
                    // 根据id查询可退库的电池
                    electricityBatteryVOList = electricityBatteryService.listEnableExitWarehouseBattery(idLongSet, tenantId, StockStatusEnum.UN_STOCK.getCode());
                    if (CollectionUtils.isEmpty(electricityBatteryVOList)) {
                        return R.fail("300817", "上传的电池编码不存在，请检测后操作");
                    }
                    
                    idLongList = electricityBatteryVOList.stream().map(ElectricityBatteryVO::getId).collect(Collectors.toList());
                    snList = electricityBatteryVOList.stream().map(ElectricityBatteryVO::getSn).collect(Collectors.toList());
                    
                } else {
                    if (Objects.equals(inventoryStatus, AssetConstant.ASSET_INVENTORY_STATUS_TAKING)) {
                        return R.fail("300806", "该加盟商车辆资产正在进行盘点，请稍后再试");
                    }
                    
                    // 根据id查询可退库的车辆
                    electricityCarVOList = electricityCarService.listEnableExitWarehouseCar(idLongSet, tenantId, StockStatusEnum.UN_STOCK.getCode());
                    if (CollectionUtils.isEmpty(electricityCarVOList)) {
                        return R.fail("300818", "上传的车辆编码不存在，请检测后操作");
                    }
                    
                    idLongList = electricityCarVOList.stream().map(ElectricityCarVO::getId).collect(Collectors.toList());
                    snList = electricityCarVOList.stream().map(ElectricityCarVO::getSn).collect(Collectors.toList());
                }
                
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
                        .warehouseId(warehouseId).idIntegerList(idIntegerList).idLongList(idLongList).build();
                // 持久化
                handleExitWarehouse(assetBatchExitWarehouseRequest, type, recordSaveQueryModel, detailSaveQueryModelList, operator);
                // 清理缓存
                handleClearCache(electricityCabinetVOList, electricityBatteryVOList, electricityCarVOList);
            }
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_EXIT_WAREHOUSE_RECORD_LOCK + operator);
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void handleExitWarehouse(AssetBatchExitWarehouseRequest assetBatchExitWarehouseRequest, Integer type, AssetExitWarehouseSaveQueryModel recordSaveQueryModel,
            List<AssetExitWarehouseDetailSaveQueryModel> detailSaveQueryModelList, Long operator) {
        R result;
        if (AssetTypeEnum.ASSET_TYPE_CABINET.getCode().equals(type)) {
            // 电柜批量退库
            result = electricityCabinetV2Service.batchExitWarehouse(assetBatchExitWarehouseRequest);
        } else if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type)) {
            // 电池批量退库
            result = electricityBatteryService.batchExitWarehouse(assetBatchExitWarehouseRequest);
        } else {
            //车辆批量退库
            result = electricityCarService.batchExitWarehouse(assetBatchExitWarehouseRequest);
        }
        
        //异步记录
        if (Objects.nonNull(result) && (Integer) result.getData() > NumberConstant.ZERO) {
            executorService.execute(() -> {
                // 新增资产退库记录
                assetExitWarehouseRecordMapper.insertOne(recordSaveQueryModel);
                // 新增资产退库详情
                assetExitWarehouseDetailService.batchInsert(detailSaveQueryModelList, operator);
            });
        }
    }
    
    private void handleClearCache(List<ElectricityCabinetVO> electricityCabinetVOList, List<ElectricityBatteryVO> electricityBatteryVOList,
            List<ElectricityCarVO> electricityCarVOList) {
        //清理柜机缓存
        if (CollectionUtils.isNotEmpty(electricityCabinetVOList)) {
            if (CollectionUtils.isNotEmpty(electricityCabinetVOList)) {
                electricityCabinetVOList.forEach(electricityCabinet -> {
                    redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId());
                    redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
                });
            }
        } else if (CollectionUtils.isNotEmpty(electricityBatteryVOList)) {
            //清理电池缓存
            electricityBatteryVOList.forEach(electricityBattery -> {
                redisService.delete(CacheConstant.CACHE_BT_ATTR + electricityBattery.getSn());
            });
        } else if (CollectionUtils.isNotEmpty(electricityCarVOList)) {
            //清理车辆缓存
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
