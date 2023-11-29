package com.xiliulou.electricity.service.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.queryModel.asset.AssetBatchExitWarehouseBySnQueryModel;
import com.xiliulou.electricity.queryModel.electricityCabinet.ElectricityCabinetBatchExitWarehouseBySnQueryModel;
import com.xiliulou.electricity.request.asset.ElectricityCabinetAddRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetBatchOutWarehouseRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetOutWarehouseRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetSnSearchRequest;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

public interface ElectricityCabinetV2Service {
    Triple<Boolean, String, Object> save(ElectricityCabinetAddRequest electricityCabinetAddRequest);
    
    boolean existByProductKeyAndDeviceName(String productKey,String deviceName);
    
    Triple<Boolean, String, Object> outWarehouse(ElectricityCabinetOutWarehouseRequest outWarehouseRequest);
    
    Triple<Boolean, String, Object> batchOutWarehouse(ElectricityCabinetBatchOutWarehouseRequest batchOutWarehouseRequest);
    
    Integer existsByWarehouseId(Long wareHouseId);
    
    List<ElectricityCabinetVO> listByFranchiseeIdAndStockStatus(ElectricityCabinetSnSearchRequest electricityCabinetSnSearchRequest);
    
    R batchExitWarehouseBySn(AssetBatchExitWarehouseBySnQueryModel assetBatchExitWarehouseBySnQueryModel);
    
    List<ElectricityCabinetVO> listBySnList(List<String> snList, Integer tenantId, Long franchiseeId);
}
