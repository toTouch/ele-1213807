package com.xiliulou.electricity.controller.admin.asset;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.asset.BatteryAddRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class JsonAdminBatteryController {
    
    @Autowired
    private ElectricityBatteryService electricityBatteryService;
    
    @PostMapping(value = "/admin/battery/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) BatteryAddRequest batteryAddRequest) {
        return electricityBatteryService.saveElectricityBatteryV2(batteryAddRequest);
    }
}
