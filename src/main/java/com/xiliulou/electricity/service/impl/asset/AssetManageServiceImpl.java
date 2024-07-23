package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.electricity.bo.asset.AssetBatchExitWarehouseBO;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.enums.asset.WarehouseOperateTypeEnum;
import com.xiliulou.electricity.request.asset.AssetBatchExitWarehouseRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseDetailService;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseRecordService;
import com.xiliulou.electricity.service.asset.AssetManageService;
import com.xiliulou.electricity.service.asset.AssetWarehouseRecordService;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author HeYafeng
 * @description 资产管理
 * @date 2024/7/2 18:32:29
 */
@Slf4j
@Service
public class AssetManageServiceImpl implements AssetManageService {
    
    @Resource
    private ElectricityCabinetV2Service electricityCabinetV2Service;
    
    @Resource
    private ElectricityBatteryService electricityBatteryService;
    
    @Resource
    private ElectricityCarService electricityCarService;
    
    @Resource
    private AssetExitWarehouseDetailService assetExitWarehouseDetailService;
    
    @Resource
    private AssetWarehouseRecordService assetWarehouseRecordService;
    
    @Resource
    private AssetExitWarehouseRecordService assetExitWarehouseRecordService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchExistWarehouseTx(List<AssetBatchExitWarehouseBO> dataList) {
        AtomicBoolean flag = new AtomicBoolean(false);
        // 资产退库
        dataList.parallelStream().forEach(data -> {
            Integer count;
            Integer type = data.getType();
            AssetBatchExitWarehouseRequest assetBatchExitWarehouseRequest = data.getAssetBatchExitWarehouseRequest();
            if (CollectionUtils.isNotEmpty(assetBatchExitWarehouseRequest.getIdList())) {
                flag.set(true);
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
                
                // 记录
                if (count > NumberConstant.ZERO) {
                    Long operator = data.getOperator();
                    // 新增资产退库详情
                    assetExitWarehouseDetailService.batchInsert(data.getDetailSaveQueryModelList(), operator);
                    
                    //库房记录
                    Long warehouseId = assetBatchExitWarehouseRequest.getWarehouseId();
                    if (Objects.nonNull(warehouseId) && !Objects.equals(warehouseId, NumberConstant.ZERO_L)) {
                        
                        assetWarehouseRecordService.asyncRecordByWarehouseId(assetBatchExitWarehouseRequest.getTenantId(), operator, warehouseId, data.getSnList(), type,
                                WarehouseOperateTypeEnum.WAREHOUSE_OPERATE_TYPE_EXIT.getCode());
                    }
                }
            }
        });
        
        // 新增Record
        if (flag.get()) {
            assetExitWarehouseRecordService.insertOne(dataList.get(0).getRecordSaveQueryModel());
        }
    }
    
}
