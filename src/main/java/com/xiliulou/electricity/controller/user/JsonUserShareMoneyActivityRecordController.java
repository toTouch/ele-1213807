package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ShareMoneyActivityRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发起邀请活动记录(ShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
@RestController
public class JsonUserShareMoneyActivityRecordController {
    /**
     * 服务对象
     */
    @Autowired
    private ShareMoneyActivityRecordService shareMoneyActivityRecordService;


    /**
     * 生成分享图片
     *
     *
     */
    @PostMapping(value = "/user/shareMoneyActivityRecord/generateSharePicture")
    public R generateShareUrl(@RequestParam(value = "activityId") Integer activityId,
            @RequestParam(value = "page", required = false) String page) {
       return shareMoneyActivityRecordService.generateSharePicture(activityId,page);
    }

    /**
     * 生成分享链接
     *
     *
     */
    @PostMapping(value = "/user/shareActivityRecord/generateShareUrl")
    public R generateShareUrl(@RequestParam(value = "activityId") Integer activityId) {
        return null;
    }


}
