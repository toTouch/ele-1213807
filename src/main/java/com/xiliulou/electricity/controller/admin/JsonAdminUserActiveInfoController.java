package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.service.UserActiveInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (UserActiveInfo)表控制层
 *
 * @author Hardy
 * @since 2023-03-01 10:15:12
 */
@RestController
@RequestMapping("userActiveInfo")
public class JsonAdminUserActiveInfoController {
    
    /**
     * 服务对象
     */
    @Resource
    private UserActiveInfoService userActiveInfoService;
    
}
