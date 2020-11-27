package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBatteryModel;
import com.xiliulou.electricity.service.ElectricityBatteryModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @program: XILIULOU
 * @description: 电池型号 controller
 * @author: Mr.YG
 * @create: 2020-11-27 14:08
 **/
@RestController
@Slf4j
public class ElectricityCabinetBatteryModelAdminController {
    @Autowired
    ElectricityBatteryModelService electricityBatteryModelService;

    /**
     * @param electricityBatteryModel
     * @return
     */
    @PostMapping("admin/battery/model")
    public R saveBatteryModel(@Validated ElectricityBatteryModel electricityBatteryModel) {
        return electricityBatteryModelService.saveElectricityBatteryModel(electricityBatteryModel);
    }

    /**
     * @param electricityBatteryModel
     * @return
     */
    @PutMapping("admin/battery/model")
    public R updateBatteryModel(@Validated ElectricityBatteryModel electricityBatteryModel) {
        if (Objects.isNull(electricityBatteryModel.getId())) {
            return R.failMsg("电池型号id不能为空!");
        }
        return electricityBatteryModelService.updateElectricityBatteryModel(electricityBatteryModel);
    }


}
