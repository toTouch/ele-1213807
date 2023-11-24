package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.asset.AssetInventoryDetailBO;
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
import com.xiliulou.electricity.vo.asset.AssetInventoryDetailVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    
    @Override
    public R listByOrderNo(AssetInventoryDetailRequest assetInventoryRequest) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 模型转换
        AssetInventoryDetailQueryModel assetInventoryDetailQueryModel = new AssetInventoryDetailQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetInventoryDetailQueryModel);
        assetInventoryDetailQueryModel.setTenantId(tenantId);
        
        //如果详情表中没有电池数据，需同步电池信息到资产盘点详情表中
        List<AssetInventoryDetailVO> inventoryDetailVOList = new ArrayList<>();
        List<AssetInventoryDetailBO> inventoryDetailBOList = assetInventoryDetailMapper.selectListByOrderNo(assetInventoryDetailQueryModel);
        if (CollectionUtils.isEmpty(inventoryDetailBOList)) {
            inventoryDetailVOList = syncBatteryToInventoryDetail(assetInventoryRequest, tenantId);
        }
        
        return R.ok(inventoryDetailVOList);
    }
    
    /**
     * @description 将电池数据同步到资产详情表
     * @date 2023/11/21 14:40:16
     * @author HeYafeng
     */
    private List<AssetInventoryDetailVO> syncBatteryToInventoryDetail(AssetInventoryDetailRequest assetInventoryRequest, Integer tenantId) {
        Long franchiseeId = assetInventoryRequest.getFranchiseeId();
        List<AssetInventoryDetailVO> inventoryDetailVOList = new ArrayList<>();
        List<AssetInventoryDetailSaveQueryModel> inventoryDetailSaveQueryModelList = new ArrayList<>();
        
        ElectricityBatteryListSnByFranchiseeQueryModel queryModel = ElectricityBatteryListSnByFranchiseeQueryModel.builder().tenantId(tenantId).franchiseeId(franchiseeId)
                .size(assetInventoryRequest.getSize()).offset(assetInventoryRequest.getOffset()).build();
        List<String> snList = electricityBatteryService.listSnByFranchiseeId(queryModel);
        if (CollectionUtils.isNotEmpty(snList)) {
            inventoryDetailVOList = snList.stream().map(sn -> {
                AssetInventoryDetailVO inventoryDetailVO = new AssetInventoryDetailVO();
                
                AssetInventoryDetailSaveQueryModel inventoryDetailSaveQueryModel = AssetInventoryDetailSaveQueryModel.builder().orderNo(assetInventoryRequest.getOrderNo()).sn(sn)
                        .type(AssetTypeEnum.ASSET_TYPE_BATTERY.getCode()).franchiseeId(franchiseeId).inventoryStatus(AssetInventoryDetail.INVENTORY_STATUS_NO)
                        .status(AssetInventoryDetailStatusEnum.ASSET_INVENTORY_DETAIL_STATUS_NORMAL.getCode()).operator(assetInventoryRequest.getUid()).tenantId(tenantId)
                        .delFlag(AssetInventoryDetail.DEL_NORMAL).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
                
                inventoryDetailSaveQueryModelList.add(inventoryDetailSaveQueryModel);
                BeanUtils.copyProperties(inventoryDetailSaveQueryModel, inventoryDetailVO);
                
                return inventoryDetailVO;
                
            }).collect(Collectors.toList());
            
            // 批量新增
            if (CollectionUtils.isNotEmpty(inventoryDetailSaveQueryModelList)) {
                assetInventoryDetailMapper.batchInsert(inventoryDetailSaveQueryModelList);
            }
        }
        
        return inventoryDetailVOList;
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
    
    @Override
    public Integer countTotal(AssetInventoryDetailRequest assetInventoryRequest) {
        ElectricityBatteryQuery electricityBatteryQuery = ElectricityBatteryQuery.builder().tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(assetInventoryRequest.getFranchiseeId()).build();
        Object data = electricityBatteryService.queryCount(electricityBatteryQuery).getData();
        return (Integer) data;
    }
}
