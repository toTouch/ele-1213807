package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.service.CarLockCtrlHistoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (CarLockCtrlHistory)表控制层
 *
 * @author Hardy
 * @since 2023-04-04 16:22:29
 */
@RestController
public class JsonAdminCarLockCtrlHistoryController {
    
    /**
     * 服务对象
     */
    @Resource
    private CarLockCtrlHistoryService carLockCtrlHistoryService;
    
    
}
