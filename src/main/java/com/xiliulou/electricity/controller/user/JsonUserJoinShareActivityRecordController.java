package com.xiliulou.electricity.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@RestController
public class JsonUserJoinShareActivityRecordController {
    /**
     * 服务对象
     */
    @Resource
    private JoinShareActivityRecordService joinShareActivityRecordService;


    /**
     * 解密分享图片
     *
     */
    @GetMapping(value = "/outer/joinShareActivityRecord/checkScene")
    public R checkScene(@RequestParam(value = "scene") String scene) {
        return joinShareActivityRecordService.checkScene(scene);
    }


    /**
     * 点击分享链接进入活动
     *
     */
    @PostMapping(value = "/user/joinShareActivityRecord/joinActivity")
    public R joinActivity(@RequestParam(value = "activityId") Integer activityId,@RequestParam(value = "uid") Long uid) {
        return joinShareActivityRecordService.joinActivity(activityId,uid);
    }



}
