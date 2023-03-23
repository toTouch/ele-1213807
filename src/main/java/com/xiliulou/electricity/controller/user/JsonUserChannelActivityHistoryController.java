package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ChannelActivityHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zgw
 * @date 2023/3/23 10:55
 * @mood
 */
@RestController
@Slf4j
public class JsonUserChannelActivityHistoryController {
    
    @Autowired
    ChannelActivityHistoryService channelActivityHistoryService;
    
    /**
     * 用户获取自己的渠道活动二维码
     */
    @GetMapping("user/channelActivityHistory/queryCode")
    public R queryCode() {
        return channelActivityHistoryService.queryCode();
    }
    
    /**
     * 用户扫其他人二维码进入活动
     */
    @GetMapping("user/channelActivityHistory/scanIntoActivity")
    public R scanIntoActivity(@RequestParam("code") String code) {
        return channelActivityHistoryService.scanIntoActivity(code);
    }
    
}
