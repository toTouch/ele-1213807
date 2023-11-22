package com.xiliulou.electricity.service.asset;

import com.xiliulou.electricity.request.asset.ElectricityCabinetAddRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetBatchOutWarehouseRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetOutWarehouseRequest;
import org.apache.commons.lang3.tuple.Triple;

public interface ElectricityCabinetV2Service {
    Triple<Boolean, String, Object> save(ElectricityCabinetAddRequest electricityCabinetAddRequest);
    
    boolean existByProductKeyAndDeviceName(String productKey,String deviceName);
    
    Triple<Boolean, String, Object> outWarehouse(ElectricityCabinetOutWarehouseRequest outWarehouseRequest);
    
    Triple<Boolean, String, Object> batchOutWarehouse(ElectricityCabinetBatchOutWarehouseRequest batchOutWarehouseRequest);
}
