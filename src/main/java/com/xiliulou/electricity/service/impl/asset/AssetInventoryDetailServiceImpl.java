package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.mapper.asset.AssetInventoryDetailMapper;
import com.xiliulou.electricity.queryModel.asset.AssetInventoryDetailQueryModel;
import com.xiliulou.electricity.request.asset.AssetInventoryDetailBatchInventoryRequest;
import com.xiliulou.electricity.request.asset.AssetInventoryDetailRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.asset.AssetInventoryDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.asset.AssetInventoryDetailVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    
    @Override
    public R listByOrderNo(AssetInventoryDetailRequest assetInventoryRequest) {
        Long tenantId = TenantContextHolder.getTenantId().longValue();
        
        // 模型转换
        AssetInventoryDetailQueryModel assetInventoryDetailQueryModel = new AssetInventoryDetailQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetInventoryDetailQueryModel);
        assetInventoryDetailQueryModel.setTenantId(tenantId);
        
        //如果详情表中没有电池数据，需同步电池信息到资产盘点详情表中
        List<AssetInventoryDetailVO> inventoryDetailVOList = assetInventoryDetailMapper.selectListByOrderNo(assetInventoryDetailQueryModel);
        if (CollectionUtils.isEmpty(inventoryDetailVOList)) {
            inventoryDetailVOList = syncBatteryToInventoryDetail(assetInventoryRequest, tenantId);
        }
        
        return R.ok(inventoryDetailVOList);
    }
    
    /**
     * @description 将电池数据同步到资产详情表
     * @date 2023/11/21 14:40:16
     * @author HeYafeng
     */
    private List<AssetInventoryDetailVO> syncBatteryToInventoryDetail(AssetInventoryDetailRequest assetInventoryRequest, Long tenantId) {
        /*List<AssetInventoryDetailVO> inventoryDetailVOList = new ArrayList<>();
        Long franchiseeId = assetInventoryRequest.getFranchiseeId();
        
        ElectricityBatteryListSnByFranchiseeQueryModel queryModel = ElectricityBatteryListSnByFranchiseeQueryModel.builder().tenantId(tenantId).franchiseeId(franchiseeId)
                .size(assetInventoryRequest.getSize()).offset(assetInventoryRequest.getOffset()).build();
        List<String> snList = electricityBatteryService.listSnByFranchiseeId(queryModel);
        if (CollectionUtils.isNotEmpty(snList)) {
            List<AssetInventoryDetail> syncBatteryList = snList.stream().map(sn -> {
                AssetInventoryDetailVO inventoryDetailVO = new AssetInventoryDetailVO();
                
                AssetInventoryDetail assetInventoryDetail = AssetInventoryDetail.builder().orderNo(assetInventoryRequest.getOrderNo()).sn(sn)
                        .type(AssetTypeEnum.ASSET_TYPE_BATTERY.getCode()).franchiseeId(franchiseeId).inventoryStatus(AssetInventoryDetail.INVENTORY_STATUS_NO)
                        .status(AssetInventoryDetailStatusEnum.ASSET_INVENTORY_DETAIL_STATUS_NORMAL.getCode()).operator(assetInventoryRequest.getUid()).tenantId(tenantId)
                        .delFlag(AssetInventoryDetail.DEL_NORMAL).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
                
                BeanUtils.copyProperties(assetInventoryDetail, inventoryDetailVO);
                inventoryDetailVOList.add(inventoryDetailVO);
                
                return assetInventoryDetail;
                
            }).collect(Collectors.toList());
            
            // 批量新增
            assetInventoryDetailMapper.batchInsert(syncBatteryList);
        }
        
        return inventoryDetailVOList;*/
        return null;
    }
    
    @Override
    public R batchInventory(AssetInventoryDetailBatchInventoryRequest inventoryRequest) {
        return R.ok(assetInventoryDetailMapper.batchInventoryBySnList(inventoryRequest));
    }
    
    @Override
    public Integer queryCount(AssetInventoryDetailRequest assetInventoryRequest) {
        Long tenantId = TenantContextHolder.getTenantId().longValue();
    
        // 模型转换
        AssetInventoryDetailQueryModel assetInventoryDetailQueryModel = new AssetInventoryDetailQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetInventoryDetailQueryModel);
        assetInventoryDetailQueryModel.setTenantId(tenantId);
    
        //如果详情表中没有电池数据，需同步电池信息到资产盘点详情表中
        List<AssetInventoryDetailVO> inventoryDetailVOList = assetInventoryDetailMapper.selectListByOrderNo(assetInventoryDetailQueryModel);
        if (CollectionUtils.isEmpty(inventoryDetailVOList)) {
            inventoryDetailVOList = syncBatteryToInventoryDetail(assetInventoryRequest, tenantId);
        }
        
        return inventoryDetailVOList.size();
    }
}
