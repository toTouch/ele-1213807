package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.electricity.bo.asset.AssetBatchExitWarehouseBO;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.battery.BatteryLabelModifyDTO;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.enums.asset.WarehouseOperateTypeEnum;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.request.asset.AssetBatchExitWarehouseRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseDetailService;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseRecordService;
import com.xiliulou.electricity.service.asset.AssetManageService;
import com.xiliulou.electricity.service.asset.AssetWarehouseRecordService;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

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
    public void batchExistWarehouse(List<AssetBatchExitWarehouseBO> dataList) {
        AtomicBoolean flag = new AtomicBoolean(true);
        // 资产退库
        dataList.parallelStream().forEach(data -> {
            try {
                Integer count;
                Integer type = data.getType();
                AssetBatchExitWarehouseRequest assetBatchExitWarehouseRequest = data.getAssetBatchExitWarehouseRequest();
                if (Objects.isNull(assetBatchExitWarehouseRequest)) {
                    flag.set(false);
                    return;
                }
                
                if (CollectionUtils.isNotEmpty(assetBatchExitWarehouseRequest.getIdList())) {
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
                        List<String> snList = data.getSnList();
                        if (Objects.nonNull(warehouseId) && !Objects.equals(warehouseId, NumberConstant.ZERO_L)) {
                            
                            assetWarehouseRecordService.asyncRecordByWarehouseId(assetBatchExitWarehouseRequest.getTenantId(), operator, warehouseId, snList, type,
                                    WarehouseOperateTypeEnum.WAREHOUSE_OPERATE_TYPE_EXIT.getCode());
                        }
                        
                        // 修改电池标签
                        if (AssetTypeEnum.ASSET_TYPE_BATTERY.getCode().equals(type) && CollectionUtils.isNotEmpty(snList)) {
                            for (String sn : snList) {
                                ElectricityBattery battery = ElectricityBattery.builder().sn(sn).tenantId(TenantContextHolder.getTenantId()).build();
                                BatteryLabelModifyDTO labelModifyDTO = BatteryLabelModifyDTO.builder().newLabel(BatteryLabelEnum.INVENTORY.getCode())
                                        .operatorUid(SecurityUtils.getUid()).build();
                                electricityBatteryService.asyncModifyLabel(battery, null, labelModifyDTO, false);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                flag.set(false);
                log.error("batchExistWarehouseTx error!", e);
            }
        });
        
        // 新增Record
        if (flag.get()) {
            assetExitWarehouseRecordService.insertOne(dataList.get(0).getRecordSaveQueryModel());
        }
    }
    
}
