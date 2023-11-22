package com.xiliulou.electricity.service.asset;

import com.xiliulou.electricity.queue.asset.ElectricityCabinetAddRequest;
import com.xiliulou.electricity.queue.asset.ElectricityCabinetBatchOutWarehouseRequest;
import com.xiliulou.electricity.queue.asset.ElectricityCabinetOutWarehouseRequest;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

public interface ElectricityCabinetV2Service {
    Triple<Boolean, String, Object> save(ElectricityCabinetAddRequest electricityCabinetAddRequest);
    
    boolean existByProductKeyAndDeviceName(String productKey,String deviceName);
    
    Triple<Boolean, String, Object> outWarehouse(ElectricityCabinetOutWarehouseRequest outWarehouseRequest);
    
    Triple<Boolean, String, Object> batchOutWarehouse(ElectricityCabinetBatchOutWarehouseRequest batchOutWarehouseRequest);
}
