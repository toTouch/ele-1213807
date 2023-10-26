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
    public R queryNotificationList(@RequestParam(value = "offset",required = false)Long offset,
                                   @RequestParam(value = "size",required = false)Long size){
        return R.ok(notificationService.queryNotificationList(offset,size));
    }

    /**
     * 查询count
     * @return
     */
    @GetMapping("/admin/notification/queryCount")
    public R queryNotificationCount(){
        return notificationService.queryNotificationCount();
    }

    /**
     * 获取最新的版本信息
     */
    @GetMapping("/admin/notification/latest")
    public R queryVersionLatest(){
        return R.ok(notificationService.queryCreateTimeMaxTenantNotification());
    }
    
    /**
     * @description 获取上传通知图片所需的签名
     * @date 2023/10/26 18:16:06
     * @author HeYafeng
     */
    @GetMapping(value = "/admin/acquire/upload/versionNotification/file/sign")
    public R getUploadVersionNotificationFileSign() {
        return notificationService.acquireVersionNotificationFileSign();
    }
}
