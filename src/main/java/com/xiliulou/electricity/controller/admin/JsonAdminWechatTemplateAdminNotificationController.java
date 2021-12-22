package com.xiliulou.electricity.controller.admin;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.WechatTemplateAdminNotification;
import com.xiliulou.electricity.query.WechatTemplateAdminNotificationQuery;
import com.xiliulou.electricity.service.WechatTemplateAdminNotificationService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (WechatTemplateAdminNotification)表控制层
 *
 * @author Hardy
 * @since 2021-11-25 16:51:02
 */
@RestController
@Slf4j
public class JsonAdminWechatTemplateAdminNotificationController {
    /**
     * 服务对象
     */
    @Resource
    private WechatTemplateAdminNotificationService wechatTemplateAdminNotificationService;

    @PostMapping("/admin/adminNotification")
    public R saveOne(@RequestBody WechatTemplateAdminNotificationQuery wechatTemplateAdminNotificationQuery){
        return wechatTemplateAdminNotificationService.saveOne(wechatTemplateAdminNotificationQuery);
    }

    @PutMapping("/admin/adminNotification")
    public R updateOne(@RequestBody WechatTemplateAdminNotificationQuery wechatTemplateAdminNotificationQuery){
        return wechatTemplateAdminNotificationService.updateOne(wechatTemplateAdminNotificationQuery);
    }

    @GetMapping("/admin/adminNotification")
    public R queryList(){
        return wechatTemplateAdminNotificationService.queryList();
    }

}
