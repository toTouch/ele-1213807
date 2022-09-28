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
        @RequestParam(value = "serverBeginTime", required = false) Long serverBeginTime,
        @RequestParam(value = "serverEndTime", required = false) Long serverEndTime,
        @RequestParam("offset") Long offset, @RequestParam("size") Long size) {

        return electricityCabinetServerService
            .queryList(eleName, deviceName, tenantName, serverBeginTime, serverEndTime, offset, size);
    }

    @DeleteMapping("/delete/{id}") public R deleteOne(@PathVariable("id") Long id) {
        return electricityCabinetServerService.deleteOne(id);
    }

    @PutMapping("/update") public R updateOne(@RequestParam(value = "id") Long id,
        @RequestParam(value = "serverBeginTime") Long serverBeginTime,
        @RequestParam(value = "serverEndTime") Long serverEndTime) {
        return electricityCabinetServerService.updateOne(id, serverBeginTime, serverEndTime);
    }
}
