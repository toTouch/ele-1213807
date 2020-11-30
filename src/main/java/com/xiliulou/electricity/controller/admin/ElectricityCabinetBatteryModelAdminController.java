package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBatteryModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: XILIULOU
 * @description: 电池型号 controller
 * @author: Mr.YG
 * @create: 2020-11-27 14:08
 **/
@RestController
@Slf4j
public class ElectricityCabinetBatteryModelAdminController {

    @PostMapping("admin/battery/model")
    public R saveBatteryModel(@Validated ElectricityBatteryModel electricityBatteryModel) {
        return null;
    }

}
