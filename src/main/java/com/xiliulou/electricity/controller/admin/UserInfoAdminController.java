package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.entity.EleUserAuth;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Objects;

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
    @Autowired
    UserInfoService userInfoService;


    //绑定电池
    @PutMapping(value = "/admin/userInfo/bindBattery")
    public R bindBattery(@RequestBody @Validated(value = UpdateGroup.class) UserInfoBatteryAddAndUpdate userInfoBatteryAddAndUpdate) {
        return userInfoService.bindBattery(userInfoBatteryAddAndUpdate);
    }

    //解绑电池
    @PutMapping(value = "/admin/userInfo/unBindBattery/{id}")
    public R unBindBattery(@PathVariable("id") Long id) {
        return userInfoService.unBindBattery(id);
    }



    //列表查询
    @GetMapping(value = "/admin/userInfo/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "serviceStatus", required = false) Integer serviceStatus) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .serviceStatus(serviceStatus).build();

        return userInfoService.queryList(userInfoQuery);
    }

    //禁用
    @PostMapping(value = "/admin/userInfo/disable/{id}")
    public R disable(@PathVariable("id") Long id) {
        return userInfoService.disable(id);
    }


    //启用
    @PostMapping(value = "/admin/userInfo/reboot/{id}")
    public R reboot(@PathVariable("id") Long id) {
        return userInfoService.reboot(id);
    }

    //后台审核实名认证
    @PostMapping(value = "/admin/userInfo/verifyAuth")
    public R verifyAuth(@RequestParam("id") Long id,@RequestParam("authStatus") Integer authStatus) {
        return userInfoService.verifyAuth(id,authStatus);
    }

    //编辑实名认证
    @PutMapping(value = "/admin/userInfo")
    public R updateAuth(@RequestBody UserInfo userInfo) {
        return userInfoService.updateAuth(userInfo);
    }

}