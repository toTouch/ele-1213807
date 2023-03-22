package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.service.UserChannelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (UserChannel)表控制层
 *
 * @author Hardy
 * @since 2023-03-22 15:34:58
 */
@RestController
public class JsonAdminUserChannelController {
    
    /**
     * 服务对象
     */
    @Resource
    private UserChannelService userChannelService;
    
    
}
