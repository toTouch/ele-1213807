package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
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
    
    @GetMapping("admin/carLockCtrlHistory/list")
    public R queryList(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "carSn", required = false) String carSn,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
        return carLockCtrlHistoryService.queryList(offset, size, name, phone, carSn, beginTime, endTime);
    }
    
    @GetMapping("admin/carLockCtrlHistory/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "carSn", required = false) String carSn,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
        return carLockCtrlHistoryService.queryCount(name, phone, carSn, beginTime, endTime);
    }
}
