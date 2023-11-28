package com.xiliulou.electricity.service.impl.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.asset.AssetInventoryDetailBO;
import com.xiliulou.electricity.entity.asset.AssetInventoryDetail;
import com.xiliulou.electricity.enums.asset.AssetInventoryDetailStatusEnum;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.mapper.asset.AssetInventoryDetailMapper;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 资产盘点详情业务
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
    public List<AssetInventoryDetailVO> listByOrderNo(AssetInventoryDetailRequest assetInventoryRequest) {
        // 模型转换
        AssetInventoryDetailQueryModel assetInventoryDetailQueryModel = new AssetInventoryDetailQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetInventoryDetailQueryModel);
        assetInventoryDetailQueryModel.setTenantId(TenantContextHolder.getTenantId());
    
        List<AssetInventoryDetailVO> rspList = new ArrayList<>();
        
        List<AssetInventoryDetailBO> assetInventoryDetailBOList = assetInventoryDetailMapper.selectListByOrderNo(assetInventoryDetailQueryModel);
        if (CollectionUtils.isNotEmpty(assetInventoryDetailBOList)) {
            rspList = assetInventoryDetailBOList.stream().map(item -> {
                
                AssetInventoryDetailVO assetInventoryDetailVO = new AssetInventoryDetailVO();
                BeanUtils.copyProperties(item, assetInventoryDetailVO);
            
                return assetInventoryDetailVO;
            
            }).collect(Collectors.toList());
        }
    
        return rspList;
    }
    
    @Slave
    @Override
    public Integer countTotal(AssetInventoryDetailRequest assetInventoryRequest) {
        // 模型转换
        AssetInventoryDetailQueryModel assetInventoryDetailQueryModel = new AssetInventoryDetailQueryModel();
        BeanUtils.copyProperties(assetInventoryRequest, assetInventoryDetailQueryModel);
        assetInventoryDetailQueryModel.setTenantId(TenantContextHolder.getTenantId());
        
        return assetInventoryDetailMapper.countTotal(assetInventoryDetailQueryModel);
    }
    
    /**
     * @description 异步执行：将电池数据导入到资产详情表
     * @date 2023/11/21 14:40:16
     * @author HeYafeng
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
    public R batchInventory(AssetInventoryDetailBatchInventoryRequest inventoryRequest, Long operator) {
        
        Integer count = 0;
        if (CollectionUtils.isNotEmpty(inventoryRequest.getSnList())) {
            //批量盘点
            count = assetInventoryDetailMapper.batchInventoryBySnList(inventoryRequest);
            
            //同步盘点数据
            AssetInventoryUpdateDataQueryModel assetInventoryUpdateDataQueryModel = AssetInventoryUpdateDataQueryModel.builder().tenantId(TenantContextHolder.getTenantId())
                    .orderNo(inventoryRequest.getOrderNo()).inventoryCount(inventoryRequest.getSnList().size()).operator(operator)
                    .updateTime(System.currentTimeMillis()).build();
            
            assetInventoryService.updateByOrderNo(assetInventoryUpdateDataQueryModel);
        }
        
        return R.ok(count);
    }
}
