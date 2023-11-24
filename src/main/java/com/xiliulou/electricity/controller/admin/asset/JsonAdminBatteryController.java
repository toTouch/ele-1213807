package com.xiliulou.electricity.controller.admin.asset;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.asset.ElectricityCabinetAddRequest;
import com.xiliulou.electricity.validator.CreateGroup;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsonAdminBatteryController {
    
    @PostMapping(value = "/admin/electricityCabinet/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) ElectricityCabinetAddRequest electricityCabinetAddRequest) {
     
        return null;
    }
}
