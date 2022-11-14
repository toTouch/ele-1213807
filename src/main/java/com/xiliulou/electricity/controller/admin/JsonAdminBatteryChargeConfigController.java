package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.BatteryChargeConfigQuery;
import com.xiliulou.electricity.service.BatteryChargeConfigService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * app电池充电设置
 *
 * @author zzlong
 * @since 2022-08-12 14:49:37
 */
@RestController
@RequestMapping("/admin/")
public class JsonAdminBatteryChargeConfigController {

    @Autowired
    private BatteryChargeConfigService batteryChargeConfigService;

    /**
     * 根据柜机id获取
     *
     * @param electricityCabinetId
     * @return
     */
    @GetMapping("batteryChargeConfig/{electricityCabinetId}")
    public R selectByElectricityCabinetId(@PathVariable("electricityCabinetId") Long electricityCabinetId) {
        return R.ok(this.batteryChargeConfigService.selectByElectricityCabinetId(electricityCabinetId));
    }

    @GetMapping("batteryChargeConfig/{id}")
    public R selectById(@PathVariable("id") Long id) {
        return R.ok(this.batteryChargeConfigService.selectByIdFromDB(id));
    }

    @PostMapping("batteryChargeConfig")
    public R insert(@RequestBody @Validated(CreateGroup.class) BatteryChargeConfigQuery query) {
        return R.ok(this.batteryChargeConfigService.insert(query));
    }

    @PutMapping("batteryChargeConfig")
    public R update(@RequestBody @Validated(UpdateGroup.class) BatteryChargeConfigQuery query) {
        return R.ok(this.batteryChargeConfigService.update(query));
    }

    @DeleteMapping("batteryChargeConfig/{id}")
    public R update(@PathVariable("id") Long id) {
        return R.ok(this.batteryChargeConfigService.deleteById(id));
    }

    /**
     * 保存或更新
     */
    @PostMapping("batteryChargeConfig/save")
    public R atomicUpdate(@RequestBody @Validated BatteryChargeConfigQuery query) {
        return R.ok(this.batteryChargeConfigService.insertOrUpdate(query));
    }

}
