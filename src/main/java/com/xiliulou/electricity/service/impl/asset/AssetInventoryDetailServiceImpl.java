package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.asset.AssetInventoryDetail;
import com.xiliulou.electricity.enums.asset.AssetInventoryDetailStatusEnum;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.mapper.asset.AssetInventoryDetailMapper;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryDetailQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryDetailSaveQueryModel;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryUpdateDataQueryModel;
import com.xiliulou.electricity.queryModel.electricityBattery.ElectricityBatteryListSnByFranchiseeQueryModel;
import com.xiliulou.electricity.request.asset.AssetInventoryDetailBatchInventoryRequest;
import com.xiliulou.electricity.request.asset.AssetInventoryDetailRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.asset.AssetInventoryDetailService;
import com.xiliulou.electricity.service.asset.AssetInventoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 资产盘点详情服务
 * @date 2023/11/20 17:25:44
 */

@Service
public class AssetInventoryDetailServiceImpl implements AssetInventoryDetailService {
    
    @Autowired
    private AssetInventoryDetailMapper assetInventoryDetailMapper;
    
    @Autowired
    private ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    private AssetInventoryService assetInventoryService;
    
    @Slave
    @Override
    public R listByOrderNo(AssetInventoryDetailRequest assetInventoryRequest) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 模型转换
        AssetInventoryDetailQueryModel assetInventoryDetailQueryModel = new AssetInventoryDetailQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetInventoryDetailQueryModel);
        assetInventoryDetailQueryModel.setTenantId(tenantId);
        
        return R.ok(assetInventoryDetailMapper.selectListByOrderNo(assetInventoryDetailQueryModel));
    }
    
    /**
     * @description 异步执行：将电池数据导入到资产详情表
     * @date 2023/11/21 14:40:16
     * @author HeYafeng
     */
    public Integer asyncBatteryProcess(ElectricityBatteryListSnByFranchiseeQueryModel queryModel, String orderNo, Long operator) {
        List<String> snList = electricityBatteryService.listSnByFranchiseeId(queryModel);
        if (CollectionUtils.isNotEmpty(snList)) {
            List<AssetInventoryDetailSaveQueryModel> inventoryDetailSaveQueryModelList = snList.stream().map(sn -> {
                
                AssetInventoryDetailSaveQueryModel inventoryDetailSaveQueryModel = AssetInventoryDetailSaveQueryModel.builder().orderNo(orderNo).sn(sn)
                        .type(AssetTypeEnum.ASSET_TYPE_BATTERY.getCode()).franchiseeId(queryModel.getFranchiseeId()).inventoryStatus(AssetInventoryDetail.INVENTORY_STATUS_NO)
                        .status(AssetInventoryDetailStatusEnum.ASSET_INVENTORY_DETAIL_STATUS_NORMAL.getCode()).operator(operator).tenantId(queryModel.getTenantId())
                        .delFlag(AssetInventoryDetail.DEL_NORMAL).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
                
                return inventoryDetailSaveQueryModel;
                
            }).collect(Collectors.toList());
            
            // 批量新增
            if (CollectionUtils.isNotEmpty(inventoryDetailSaveQueryModelList)) {
                assetInventoryDetailMapper.batchInsert(inventoryDetailSaveQueryModelList);
            }
        }
        return snList.size();
    }
    
    @Override
    public R batchInventory(AssetInventoryDetailBatchInventoryRequest inventoryRequest) {
        
        Integer count = 0;
        if (CollectionUtils.isNotEmpty(inventoryRequest.getSnList())) {
            //批量盘点
            count = assetInventoryDetailMapper.batchInventoryBySnList(inventoryRequest);
            
            //同步盘点数据
            AssetInventoryUpdateDataQueryModel assetInventoryUpdateDataQueryModel = AssetInventoryUpdateDataQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                    .orderNo(inventoryRequest.getOrderNo()).inventoryCount(inventoryRequest.getSnList().size()).operator(inventoryRequest.getUid())
                    .updateTime(System.currentTimeMillis()).build();
            
            assetInventoryService.updateByOrderNo(assetInventoryUpdateDataQueryModel);
        }
        
        return R.ok(count);
    }
    
    @Slave
    @Override
    public Integer countTotal(AssetInventoryDetailRequest assetInventoryRequest) {
        ElectricityBatteryQuery electricityBatteryQuery = ElectricityBatteryQuery.builder().tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(assetInventoryRequest.getFranchiseeId()).build();
        Object data = electricityBatteryService.queryCount(electricityBatteryQuery).getData();
        return (Integer) data;
    }
}
