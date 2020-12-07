package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBatteryModel;
import com.xiliulou.electricity.service.ElectricityBatteryModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public R saveBatteryModel(@RequestBody @Validated ElectricityBatteryModel electricityBatteryModel) {
        return electricityBatteryModelService.saveElectricityBatteryModel(electricityBatteryModel);
    }

    /**
     * @param electricityBatteryModel
     * @return
     */
    @PutMapping("admin/battery/model")
    public R updateBatteryModel(@RequestBody @Validated ElectricityBatteryModel electricityBatteryModel) {
        if (Objects.isNull(electricityBatteryModel.getId())) {
            return R.failMsg("电池型号id不能为空!");
        }
        return electricityBatteryModelService.updateElectricityBatteryModel(electricityBatteryModel);
    }

    /**
     * @param
     * @return
     */
    @DeleteMapping("admin/battery/model/{id}")
    public R delBatteryModel(@PathVariable("id") Integer id) {

        return electricityBatteryModelService.delElectricityBatteryModelById(id);
    }

    /**
     * 分页
     *
     * @param
     * @return
     */
    @GetMapping("admin/battery/model/page")
    public R getBatteryModelPage(@RequestParam(value = "offset", required = true) Long offset,
                                 @RequestParam(value = "size", required = true) Long size,
                                 @RequestParam(value = "name", required = false) String name) {

        return electricityBatteryModelService.getElectricityBatteryModelPage(offset, size, name);
    }


}
