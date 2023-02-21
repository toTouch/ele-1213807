package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserNotify;
import com.xiliulou.electricity.query.UserNotifyQuery;
import com.xiliulou.electricity.service.UserNotifyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (UserNotify)表控制层
 *
 * @author Hardy
 * @since 2023-02-21 09:10:42
 */
@RestController
public class JsonAdminUserNotifyController {
    
    /**
     * 服务对象
     */
    @Resource
    private UserNotifyService userNotifyService;
    
    @GetMapping("/admin/userNotify")
    public R queryOne() {
        return R.ok(this.userNotifyService.queryOne());
    }
    
    @DeleteMapping("/admin/userNotify/{id}")
    public R deleteOne(@PathVariable Long id) {
        return this.userNotifyService.deleteOne(id);
    }
    
    @PostMapping("/admin/userNotify")
    public R editOne(@RequestBody UserNotifyQuery userNotifyQuery) {
        return this.userNotifyService.editOne(userNotifyQuery);
    }
}
