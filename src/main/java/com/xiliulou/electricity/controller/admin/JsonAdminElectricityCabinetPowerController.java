package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ElectricityCabinetPowerQuery;
import com.xiliulou.electricity.service.ElectricityCabinetPowerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.Objects;


/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@RestController
@Slf4j
public class JsonAdminElectricityCabinetPowerController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetPowerService electricityCabinetPowerService;




    //列表查询
    @GetMapping(value = "/admin/electricityCabinetPower/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
                       @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName,
                       @RequestParam(value = "date", required = false) LocalDate date) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        ElectricityCabinetPowerQuery electricityCabinetPowerQuery = ElectricityCabinetPowerQuery.builder()
                .offset(offset)
                .size(size)
                .electricityCabinetId(electricityCabinetId)
                .electricityCabinetName(electricityCabinetName)
                .date(date)
                .build();

        return electricityCabinetPowerService.queryList(electricityCabinetPowerQuery);
    }



}
