package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.BatteryReportQuery;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.electricity.query.jt808.CarPositionReportQuery;
import com.xiliulou.electricity.service.ElectricityCarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zgw
 * @date 2023/2/16 11:07
 * @mood
 */
@RestController
@Slf4j
public class JsonOuterElectricityCarController {
    
    @Autowired
    ElectricityCarService electricityCarService;
    
    @PostMapping(value = "/outer/jt808/car/positionReport")
    public R positionReport(@RequestBody CarPositionReportQuery carPositionReportQuery) {
        return electricityCarService.positionReport(carPositionReportQuery);
    }
}
