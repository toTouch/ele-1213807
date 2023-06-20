package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-20-11:30
 */
@Slf4j
@RestController
public class JsonUserActivityInfoController extends BaseController {

    @Autowired
    private ActivityService activityService;

    /**
     * 获取是否是活动邀请人
     */
    @GetMapping("/user/activity/info")
    public R userActivityInfo() {
        return returnTripleResult(activityService.userActivityInfo());
    }
}
