package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetTraffic;
import com.xiliulou.electricity.query.ElectricityCabinetPowerQuery;
import com.xiliulou.electricity.service.ElectricityCabinetTrafficService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * zgw
 */
@RestController
@Slf4j
public class JsonAdminElectricityCabinetTrafficController {

    @Autowired
    ElectricityCabinetTrafficService electricityCabinetTrafficService;

    //列表查询
    @GetMapping(value = "/admin/electricityCabinetTraffic/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
                       @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName,
                       @RequestParam(value = "date", required = false) LocalDate date) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        return electricityCabinetTrafficService.queryList(size, offset, electricityCabinetId, electricityCabinetName, date);
    }
}
