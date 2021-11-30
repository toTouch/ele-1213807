package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-09 17:20
 **/
@RestController
@Slf4j
public class JsonUserElectricityCabinetBatteryController {


    @Autowired
    ElectricityBatteryService electricityBatteryService;

    @GetMapping("user/battery")
    public R getSelfBattery() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }

        return R.ok(electricityBatteryService.queryByUid(uid));
    }

    @GetMapping("user/battery/outTime/Info")
    public R batteryOutTimeInfo(@RequestParam("tenantId") Long tenantId){
        return electricityBatteryService.batteryOutTimeInfo(tenantId);
    }
}
