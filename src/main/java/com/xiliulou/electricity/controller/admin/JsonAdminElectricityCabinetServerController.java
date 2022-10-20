package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.ElectricityCabinetServer;
import com.xiliulou.electricity.service.ElectricityCabinetServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (ElectricityCabinetServer)表控制层
 *
 * @author zgw
 * @since 2022-09-26 11:40:36
 */
@RestController @Slf4j
public class JsonAdminElectricityCabinetServerController {
    /**
     * 服务对象
     */
    @Resource private ElectricityCabinetServerService electricityCabinetServerService;

    @GetMapping("admin/electricityCabinetServer/queryList")
    public R queryList(@RequestParam(value = "eleName", required = false) String eleName,
        @RequestParam(value = "deviceName", required = false) String deviceName,
        @RequestParam(value = "tenantName", required = false) String tenantName,
        @RequestParam(value = "serverBeginTime", required = false) Long serverBeginTime,
        @RequestParam(value = "serverEndTime", required = false) Long serverEndTime,
        @RequestParam("offset") Long offset, @RequestParam("size") Long size) {

        return electricityCabinetServerService
            .queryList(eleName, deviceName, tenantName, serverBeginTime, serverEndTime, offset, size);
    }

    @DeleteMapping("admin/electricityCabinetServer/delete/{id}")
    @Log(title = "删除电柜服务")
    public R deleteOne(@PathVariable("id") Long id) {
        return electricityCabinetServerService.deleteOne(id);
    }

    @PutMapping("admin/electricityCabinetServer/update")
    @Log(title = "修改电柜服务")
    public R updateOne(@RequestParam(value = "id") Long id,
        @RequestParam(value = "serverBeginTime") Long serverBeginTime,
        @RequestParam(value = "serverEndTime") Long serverEndTime) {
        return electricityCabinetServerService.updateOne(id, serverBeginTime, serverEndTime);
    }
}
