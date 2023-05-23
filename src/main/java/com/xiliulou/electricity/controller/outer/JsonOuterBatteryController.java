package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.BatteryReportQuery;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;


/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Slf4j
@RestController
@RefreshScope
public class JsonOuterBatteryController {

    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    BatteryModelService batteryModelService;

    /**
     * 电池电量上报
     *
     * @return
     */
    @PostMapping(value = "/outer/battery/info/report")
    @Deprecated
    public R batteryReport(@RequestBody BatteryReportQuery batteryReportQuery) {
        return electricityCabinetService.batteryReport(batteryReportQuery);
    }


    /**
     * 电池型号&电池材质
     */
    @GetMapping("/outer/battery/model/{tenantId}")
    public R batteryModels(@PathVariable("tenantId") Integer tenantId) {
        return R.ok(batteryModelService.selectBatteryModels(tenantId));
    }

}
