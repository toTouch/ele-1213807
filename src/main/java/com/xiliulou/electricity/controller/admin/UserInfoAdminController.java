package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.service.UserInfoService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 用户列表(TUserInfo)表控制层
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@RestController
public class UserInfoAdminController {
    /**
     * 服务对象
     */
    @Resource
    private UserInfoService userInfoService;

}