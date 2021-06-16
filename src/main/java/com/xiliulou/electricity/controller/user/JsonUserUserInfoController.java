package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.UserInfoService;
import okhttp3.internal.platform.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户列表(TUserInfo)表控制层
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@RestController
public class JsonUserUserInfoController {
    /**
     * 服务对象
     */
    @Autowired
    UserInfoService userInfoService;

    //查看用户状态
    @GetMapping(value = "/user/userInfo/queryUserInfo")
    public R queryUserInfo() {
        return userInfoService.queryUserInfo();
    }





}
