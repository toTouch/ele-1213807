package com.xiliulou.electricity.controller.admin;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class ElectricityCabinetBatteryAdminController {
    @Autowired
    ElectricityBatteryService electricityBatteryService;

    /**
     * 新增电池
     *
     * @param
     * @return
     */
    @PostMapping(value = "/admin/electricity/battery")
    public R save(@RequestBody @Validated ElectricityBattery electricityBattery) {
        return electricityBatteryService.save(electricityBattery);
    }
}
