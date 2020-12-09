package com.xiliulou.electricity.controller.admin;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @program: XILIULOU
 * @description: 电池 controller
 * @author: Mr.YG
 * @create: 2020-11-27 14:08
 **/
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
    @PostMapping(value = "/admin/battery")
    public R save(@RequestBody @Validated ElectricityBattery electricityBattery) {

        return electricityBatteryService.saveElectricityBattery(electricityBattery);
    }

    /**
     * 修改电池
     *
     * @param
     * @return
     */
    @PutMapping(value = "/admin/battery")
    public R update(@RequestBody @Validated ElectricityBattery electricityBattery) {
        if (Objects.isNull(electricityBattery.getId())) {
            return R.fail("请求参数错误!");
        }

        return electricityBatteryService.update(electricityBattery);
    }

    /**
     * 删除电池
     *
     * @param
     * @return
     */
    @DeleteMapping(value = "/admin/battery/{id}")
    public R delete(@PathVariable("id") Long id) {
        return electricityBatteryService.deleteElectricityBattery(id);
    }

    /**
     * 电池分页
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/page")
    public R getElectricityBatteryPage(@RequestParam(value = "offset", required = true) Long offset,
                                       @RequestParam(value = "size", required = true) Long size,
                                       ElectricityBatteryQuery electricityBatteryQuery) {
        return electricityBatteryService.getElectricityBatteryPage(electricityBatteryQuery, offset, size);
    }
}
