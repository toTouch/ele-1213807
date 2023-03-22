package com.xiliulou.electricity.controller.admin;


import com.xiliulou.electricity.entity.ChannelActivity;
import com.xiliulou.electricity.service.ChannelActivityService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (ChannelActivity)表控制层
 *
 * @author zgw
 * @since 2023-03-22 10:42:57
 */
@RestController
public class JsonAdminChannelActivityController {
    
    /**
     * 服务对象
     */
    @Resource
    private ChannelActivityService channelActivityService;
    
}
