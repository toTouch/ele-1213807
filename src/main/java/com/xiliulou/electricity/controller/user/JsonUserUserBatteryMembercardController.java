package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-13-19:54
 */
@RestController
public class JsonUserUserBatteryMembercardController extends BaseController {

    @Autowired
    private UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;

    /**
     * 用户资源包转换
     * @return
     */
    @GetMapping(value = "/user/userBatteryMembercard/transform")
    public R userBatteryMembercardTransform() {
        return returnTripleResult(userBatteryMemberCardPackageService.batteryMembercardTransform(SecurityUtils.getUid()));
    }



}
