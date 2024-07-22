package com.xiliulou.electricity.service.asset;

import com.xiliulou.electricity.query.asset.AssetEnableExitWarehouseQueryModel;
import com.xiliulou.electricity.request.asset.AssetBatchExitWarehouseRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetAddRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetBatchOutWarehouseRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetBatchUpdateFranchiseeAndStoreRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetEnableAllocateRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetOutWarehouseRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetSnSearchRequest;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Set;

public interface ElectricityCabinetV2Service {
    Triple<Boolean, String, Object> save(ElectricityCabinetAddRequest electricityCabinetAddRequest);
    
    boolean existByProductKeyAndDeviceName(String productKey,String deviceName);
    
    Triple<Boolean, String, Object> outWarehouse(ElectricityCabinetOutWarehouseRequest outWarehouseRequest);
    
    Triple<Boolean, String, Object> batchOutWarehouse(ElectricityCabinetBatchOutWarehouseRequest batchOutWarehouseRequest);
    
    Integer existsByWarehouseId(Long wareHouseId);
    
    List<ElectricityCabinetVO> listByFranchiseeIdAndStockStatus(ElectricityCabinetSnSearchRequest electricityCabinetSnSearchRequest);
    
    Integer batchExitWarehouse(AssetBatchExitWarehouseRequest assetBatchExitWarehouseRequest);
    
    Integer batchUpdateFranchiseeIdAndStoreId(List<ElectricityCabinetBatchUpdateFranchiseeAndStoreRequest> batchUpdateFranchiseeAndStoreRequestList);
    
    List<ElectricityCabinetVO> listEnableAllocateCabinet(ElectricityCabinetEnableAllocateRequest enableAllocateRequest);
    
    List<ElectricityCabinetVO> listEnableExitWarehouseCabinet(AssetEnableExitWarehouseQueryModel queryModel);
    
    List<ElectricityCabinetVO> listBySnList(List<String> snList, Integer tenantId, Long franchiseeId);
    
    Integer reloadEleCabinetGeo();
}
