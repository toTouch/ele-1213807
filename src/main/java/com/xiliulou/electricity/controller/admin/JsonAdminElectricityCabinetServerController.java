package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetServer;
import com.xiliulou.electricity.service.ElectricityCabinetServerService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (ElectricityCabinetServer)表控制层
 *
 * @author zgw
 * @since 2022-09-26 11:40:36
 */
@RestController @RequestMapping("admin/electricityCabinetServer/")
public class JsonAdminElectricityCabinetServerController {
    /**
     * 服务对象
     */
    @Resource private ElectricityCabinetServerService electricityCabinetServerService;

    @GetMapping("queryList") public R queryList(@RequestParam(value = "eleName", required = false) String eleName,
        @RequestParam(value = "deviceName", required = false) String deviceName,
        @RequestParam(value = "tenantName", required = false) String tenantName,
        @RequestParam(value = "serverTimeStart", required = false) Long serverTimeStart,
        @RequestParam(value = "serverTimeEnd", required = false) Long serverTimeEnd,
        @RequestParam("offset") Long offset, @RequestParam("size") Long size) {

        return electricityCabinetServerService
            .queryList(eleName, deviceName, tenantName, serverTimeStart, serverTimeEnd, offset, size);
    }

    @DeleteMapping("/delete/{id}") public R deleteOne(@PathVariable("id") Long id) {
        return electricityCabinetServerService.deleteOne(id);
    }

    @PutMapping("/update") public R updateOne(@RequestParam(value = "id") Long id,
        @RequestParam(value = "serverTimeStart") Long serverTimeStart,
        @RequestParam(value = "serverTimeEnd") Long serverTimeEnd) {
        return electricityCabinetServerService.updateOne(id, serverTimeStart, serverTimeEnd);
    }
}
