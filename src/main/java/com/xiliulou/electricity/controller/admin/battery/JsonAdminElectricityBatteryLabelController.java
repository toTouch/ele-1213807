package com.xiliulou.electricity.controller.admin.battery;

import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author SJP
 * @date 2025-02-18 14:20
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/battery/label")
public class JsonAdminElectricityBatteryLabelController {
    
    private final ElectricityBatteryLabelService electricityBatteryLabelService;
    
    
    
}
