package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.VersionNotificationQuery;
import com.xiliulou.electricity.service.VersionNotificationService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

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

    /**
     * 编辑版本通知
     * @param versionNotificationQuery
     * @return
     */
    @PutMapping("/admin/notification")
    public R updateNotification(@RequestBody @Validated VersionNotificationQuery versionNotificationQuery) {
        return returnTripleResult(notificationService.updateNotification(versionNotificationQuery));
    }

    /**
     * 新增版本通知
     * @return
     */
    @PostMapping("/admin/notification")
    public R addNotification(@RequestBody @Validated VersionNotificationQuery versionNotificationQuery){
        return returnTripleResult(notificationService.insertNotification(versionNotificationQuery));
    }

    /**
     * 查询版本通知列表
     * @return
     */
    @GetMapping("/admin/notification/list")
    public R queryNotificationList(){
        return R.ok(notificationService.queryNotificationList());
    }
}
