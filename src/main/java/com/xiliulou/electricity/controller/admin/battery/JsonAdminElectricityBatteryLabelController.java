package com.xiliulou.electricity.controller.admin.battery;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.battery.BatteryLabelBatchUpdateRequest;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelBizService;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    
    private final ElectricityBatteryLabelBizService electricityBatteryLabelBizService;
    
    
    @GetMapping("/updateRemark")
    public R updateRemark(@RequestParam String sn, @RequestParam String remark) {
        return R.ok(electricityBatteryLabelBizService.updateRemark(sn, remark));
    }
}
