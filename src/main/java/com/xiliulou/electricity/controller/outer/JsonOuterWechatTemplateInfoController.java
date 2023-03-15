package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Hardy
 * @date 2021/12/3 9:30
 * @mood
 */
@RestController
public class JsonOuterWechatTemplateInfoController {

    @Autowired
    ElectricityBatteryService electricityBatteryService;

    @GetMapping("outer/battery/outTime/Info")
    @Deprecated
    public R batteryOutTimeInfo(@RequestParam("tenantId") Long tenantId){
        return electricityBatteryService.batteryOutTimeInfo(tenantId);
    }
}
