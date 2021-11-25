package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 活动表(TActivity)表控制层
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@RestController
@Slf4j
public class JsonUserShareMoneyActivityController {
    /**
     * 服务对象
     */
    @Autowired
    private ShareMoneyActivityService shareMoneyActivityService;



    //查询活动详情
    @GetMapping(value = "/user/shareMoneyActivity/activityInfo")
    public R queryInfo() {
        return shareMoneyActivityService.activityInfo();
    }
}
