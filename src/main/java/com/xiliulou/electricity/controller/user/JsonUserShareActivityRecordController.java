package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 发起邀请活动记录(ShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
@RestController
@RequestMapping("shareActivityRecord")
public class JsonUserShareActivityRecordController {
    /**
     * 服务对象
     */
    @Autowired
    private ShareActivityRecordService shareActivityRecordService;


    /**
     * 生成分享链接
     *
     * type 1--分享图片  2--分享链接
     */
    @GetMapping(value = "/shareActivityRecord/generateShareUrl")
    public R generateShareUrl(@RequestParam(value = "activityId") Integer activityId,@RequestParam(value = "type") Integer type,
            @RequestParam(value = "page", required = false) String page) {
       return shareActivityRecordService.generateShareUrl(activityId,type,page);
    }

}
