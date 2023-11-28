package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.asset.AssetExitWarehouseBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.asset.AssetExitWarehouseDetail;
import com.xiliulou.electricity.entity.asset.AssetExitWarehouseRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.mapper.asset.AssetExitWarehouseRecordMapper;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseDetailSaveQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetExitWarehouseSaveQueryModel;
import com.xiliulou.electricity.queryModel.electricityCabinet.ElectricityCabinetBatchExitWarehouseBySnQueryModel;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseRecordRequest;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseSaveRequest;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseDetailService;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseRecordService;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.ElectricityCarVO;
import com.xiliulou.electricity.vo.asset.AssetExitWarehouseVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 退库业务
 * @date 2023/11/27 09:36:23
 */
@Service
public class AssetExitWarehouseRecordServiceImpl implements AssetExitWarehouseRecordService {
    
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
    
    @Override
    public R save(AssetExitWarehouseSaveRequest assetExitWarehouseSaveRequest) {
        
        boolean result = redisService.setNx(CacheConstant.CACHE_ASSET_EXIT_WAREHOUSE_RECORD_LOCK + assetExitWarehouseSaveRequest.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
    
        try {
            if (CollectionUtils.isNotEmpty(assetExitWarehouseSaveRequest.getSnList())) {
                Integer tenantId = TenantContextHolder.getTenantId();
                Long franchiseeId = assetExitWarehouseSaveRequest.getFranchiseeId();
                Long storeId = assetExitWarehouseSaveRequest.getStoreId();
                Integer type = assetExitWarehouseSaveRequest.getType();
                Long warehouseId = assetExitWarehouseSaveRequest.getWarehouseId();
                Long operator = assetExitWarehouseSaveRequest.getUid();
                List<String> snList = assetExitWarehouseSaveRequest.getSnList();
                String orderNo = OrderIdUtil.generateBusinessOrderId(BusinessType.ASSET_EXIT_WAREHOUSE, assetExitWarehouseSaveRequest.getUid());
                Long nowTime = System.currentTimeMillis();
                
                // 封装资产退库记录数据
                AssetExitWarehouseSaveQueryModel assetExitWarehouseSaveQueryModel = AssetExitWarehouseSaveQueryModel
                        .builder()
                        .orderNo(orderNo)
                        .franchiseeId(franchiseeId)
                        .storeId(storeId)
                        .type(type)
                        .warehouseId(warehouseId)
                        .operator(operator)
                        .tenantId(tenantId)
                        .delFlag(AssetExitWarehouseRecord.DEL_NORMAL)
                        .createTime(nowTime)
                        .updateTime(nowTime)
                        .build();
                
                // 封装资产退库详情数据
                AssetExitWarehouseDetailSaveQueryModel detailSaveQueryModel = AssetExitWarehouseDetailSaveQueryModel
                        .builder()
                        .orderNo(orderNo)
                        .type(type)
                        .tenantId(tenantId)
                        .delFlag(AssetExitWarehouseDetail.DEL_NORMAL)
                        .createTime(nowTime)
                        .updateTime(nowTime)
                        .build();
                
                List<AssetExitWarehouseDetailSaveQueryModel> detailSaveQueryModelList;
                detailSaveQueryModelList = snList.stream().map(sn -> {
                    AssetExitWarehouseDetailSaveQueryModel assetExitWarehouseDetailSaveQueryModel = new AssetExitWarehouseDetailSaveQueryModel();
                    BeanUtils.copyProperties(assetExitWarehouseDetailSaveQueryModel, detailSaveQueryModel);
                    assetExitWarehouseDetailSaveQueryModel.setSn(sn);
                    
                    return assetExitWarehouseDetailSaveQueryModel;
                    
                }).collect(Collectors.toList());
                
                // 封装电池数据
                ElectricityCabinetBatchExitWarehouseBySnQueryModel exitWarehouseBySnQueryModel = ElectricityCabinetBatchExitWarehouseBySnQueryModel
                        .builder()
                        .tenantId(tenantId)
                        .franchiseeId(franchiseeId)
                        .warehouseId(warehouseId)
                        .snList(snList)
                        .build();
                
                // 入库
                handleExitWarehouse(assetExitWarehouseSaveQueryModel, detailSaveQueryModelList, exitWarehouseBySnQueryModel, operator);
                
                // 清理缓存
                handleClearCache(snList, type, tenantId, franchiseeId);
            }
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_ASSET_EXIT_WAREHOUSE_RECORD_LOCK + assetExitWarehouseSaveRequest.getUid());
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void handleExitWarehouse(AssetExitWarehouseSaveQueryModel exitWarehouseSaveQueryModel, List<AssetExitWarehouseDetailSaveQueryModel> detailSaveQueryModelList,
            ElectricityCabinetBatchExitWarehouseBySnQueryModel exitWarehouseBySnQueryModel, Long operator) {
        // 新增资产退库记录
        assetExitWarehouseRecordMapper.insertOne(exitWarehouseSaveQueryModel);
        
        // 新增资产退库详情
        assetExitWarehouseDetailService.batchInsert(detailSaveQueryModelList, operator);
        
        // 电柜批量退库
        electricityCabinetV2Service.batchExitWarehouseBySn(exitWarehouseBySnQueryModel);
        
        //TODO 电池批量退库、车辆批量退库
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
        } else if (AssetTypeEnum.ASSET_TYPE_CAR.getCode().equals(type)) {
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
    public List<AssetExitWarehouseVO> listByFranchiseeId(AssetExitWarehouseRecordRequest assetExitWarehouseRecordRequest) {
        AssetExitWarehouseQueryModel assetExitWarehouseQueryModel = new AssetExitWarehouseQueryModel();
        BeanUtils.copyProperties(assetExitWarehouseRecordRequest, assetExitWarehouseQueryModel);
        assetExitWarehouseQueryModel.setTenantId(TenantContextHolder.getTenantId());
    
        List<AssetExitWarehouseVO> rspList = new ArrayList<>();
        
        List<AssetExitWarehouseBO> assetExitWarehouseBOList = assetExitWarehouseRecordMapper.selectListByFranchiseeId(assetExitWarehouseQueryModel);
        if (CollectionUtils.isNotEmpty(assetExitWarehouseBOList)){
            rspList = assetExitWarehouseBOList.stream().map(item -> {
                
                AssetExitWarehouseVO assetExitWarehouseVO = new AssetExitWarehouseVO();
                BeanUtils.copyProperties(item, assetExitWarehouseVO);
        
                return assetExitWarehouseVO;
        
            }).collect(Collectors.toList());
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
