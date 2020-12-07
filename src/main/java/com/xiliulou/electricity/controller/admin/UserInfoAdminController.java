package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UserInfoAddAndUpdate;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.validation.annotation.Validated;
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

    //绑定电池
    @PutMapping(value = "/admin/userInfo/bindBattery")
    public R bindBattery(@RequestBody @Validated(value = UpdateGroup.class) UserInfoAddAndUpdate userInfoAddAndUpdate) {
        return userInfoService.bindBattery(userInfoAddAndUpdate);
    }

    //用户黑名单


}