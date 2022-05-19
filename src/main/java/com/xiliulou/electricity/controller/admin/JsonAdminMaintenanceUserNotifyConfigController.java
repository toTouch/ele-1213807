package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.MaintenanceUserNotifyConfigQuery;
import com.xiliulou.electricity.service.MaintenanceUserNotifyConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author : eclair
 * @date : 2022/4/12 09:25
 */
@RestController
public class JsonAdminMaintenanceUserNotifyConfigController extends BaseController {
    @Autowired
    MaintenanceUserNotifyConfigService maintenanceUserNotifyConfigService;

    @GetMapping("/admin/maintenance/notify/config")
    public R configInfo() {
        return returnPairResult(maintenanceUserNotifyConfigService.queryConfigInfo());
    }

    @PostMapping("/admin/maintenance/notify/config")
    public R saveConfig(@Validated @RequestBody MaintenanceUserNotifyConfigQuery query) {
        return returnPairResult(maintenanceUserNotifyConfigService.saveConfig(query));
    }

    @PutMapping("/admin/maintenance/notify/config")
    public R updateConfig(@RequestBody MaintenanceUserNotifyConfigQuery query) {
        return returnPairResult(maintenanceUserNotifyConfigService.updateConfig(query));
    }

//    @PostMapping("/admin/maintenace/test")
//    public R testSendMsg() {
//        return returnPairResult(maintenanceUserNotifyConfigService.testSendMsg());
//    }

}
