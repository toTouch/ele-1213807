package com.xiliulou.electricity.controller.admin.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.request.asset.ElectricityCabinetAddRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetOutWarehouseRequest;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class JsonAdminElectricityCabinetV2Controller extends BasicController {
    
    @Autowired
    private ElectricityCabinetV2Service electricityCabinetV2Service;
    
    //新增换电柜
    @PostMapping(value = "/admin/electricityCabinet/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) ElectricityCabinetAddRequest electricityCabinetAddRequest) {
        return returnTripleResult(electricityCabinetV2Service.save(electricityCabinetAddRequest));
    }
    
    //出库
    @PostMapping(value = "/admin/electricityCabinet/outWarehouse")
    public R outWarehouse(@RequestBody @Validated(value = UpdateGroup.class) ElectricityCabinetOutWarehouseRequest outWarehouseRequest) {
        return returnTripleResult(electricityCabinetV2Service.outWarehouse(outWarehouseRequest));
    }
    
}
