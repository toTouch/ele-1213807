package com.xiliulou.electricity.controller.admin;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.PageQuery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;


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

    /**
     * 修改电池
     *
     * @param
     * @return
     */
    @PutMapping(value = "/admin/electricity/battery")
    public R update(@RequestBody @Validated ElectricityBattery electricityBattery) {
        if (Objects.isNull(electricityBattery.getId())) {
            return R.fail("请求参数错误!");
        }

        return electricityBatteryService.update(electricityBattery);
    }

    /**
     * 电池分页
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/electricity/battery/page")
    public R getElectricityBatteryPage(ElectricityBatteryQuery electricityBatteryQuery, @Validated PageQuery pageQuery) {

        log.info("pageQuery");
        return electricityBatteryService.getElectricityBatteryPage(electricityBatteryQuery, pageQuery);
    }
}
