package com.xiliulou.electricity.service.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.queryModel.electricityBattery.ElectricityBatteryListSnByFranchiseeQueryModel;
import com.xiliulou.electricity.request.asset.AssetInventoryDetailBatchInventoryRequest;
import com.xiliulou.electricity.request.asset.AssetInventoryDetailRequest;
import com.xiliulou.electricity.vo.asset.AssetInventoryDetailVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产盘点详情业务
 * @date 2023/11/20 17:22:22
 */
public interface AssetInventoryDetailService {
    
    List<AssetInventoryDetailVO> listByOrderNo(AssetInventoryDetailRequest assetInventoryRequest);
    
    R batchInventory(AssetInventoryDetailBatchInventoryRequest assetInventoryDetailBatchInventoryRequest, Long operator);
    
    Integer countTotal(AssetInventoryDetailRequest assetInventoryRequest);
    
    Integer asyncBatteryProcess(ElectricityBatteryListSnByFranchiseeQueryModel queryModel, String orderNo, Long operator);
    
    List<AssetInventoryDetailVO> listBySnListAndOrderNo(List<String> snList, String orderNo);
}
