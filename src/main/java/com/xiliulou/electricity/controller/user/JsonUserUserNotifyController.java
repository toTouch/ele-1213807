package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserNotify;
import com.xiliulou.electricity.service.UserNotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zgw
 * @date 2023/2/21 11:19
 * @mood
 */
@RestController
public class JsonUserUserNotifyController {
    
    @Autowired
    UserNotifyService userNotifyService;
    
    //查看用户状态
    @GetMapping(value = "/user/userNotify/queryInfo")
    public R queryUserInfo() {
        return userNotifyService.queryOne();
    }
}
