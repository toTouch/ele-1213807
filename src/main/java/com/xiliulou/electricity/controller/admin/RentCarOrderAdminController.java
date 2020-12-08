package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.service.RentCarOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 租车记录(TRentCarOrder)表控制层
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
@RestController
public class RentCarOrderAdminController {
    /**
     * 服务对象
     */
    @Autowired
    private RentCarOrderService rentCarOrderService;

}