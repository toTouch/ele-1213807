package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserBatteryType;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-25-10:58
 */
@RestController
@Slf4j
public class JsonAdminUserBatteryTypeController extends BaseController {

    @Autowired
    private UserBatteryTypeService userBatteryTypeService;


    @GetMapping("/admin/userBatteryType/{uid}")
    public R select(@PathVariable("uid") Long uid) {
        return returnTripleResult(userBatteryTypeService.selectUserBatteryTypeByUid(uid));
    }

    @PutMapping("/admin/userBatteryType")
    public R update(@RequestBody @Validated UserBatteryType userBatteryType) {
        return returnTripleResult(userBatteryTypeService.modifyUserBatteryType(userBatteryType));
    }


}
