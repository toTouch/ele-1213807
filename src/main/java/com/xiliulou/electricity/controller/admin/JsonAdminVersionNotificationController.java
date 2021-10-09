package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.VersionNotificationQuery;
import com.xiliulou.electricity.service.VersionNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : eclair
 * @date : 2021/9/26 5:40 下午
 */
@RestController
public class JsonAdminVersionNotificationController extends BaseController {
    @Autowired
    VersionNotificationService notificationService;

    @GetMapping("/admin/notification")
    public R queryNotification() {
        return R.ok(notificationService.queryByIdFromDB(1));
    }

    @PutMapping("/admin/notification")
    public R updateNotification(@RequestBody @Validated VersionNotificationQuery versionNotificationQuery) {
        return returnTripleResult(notificationService.updateNotification(versionNotificationQuery));

    }
}
