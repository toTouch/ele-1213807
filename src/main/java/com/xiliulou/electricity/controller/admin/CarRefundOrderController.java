package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.service.CarRefundOrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (CarRefundOrder)表控制层
 *
 * @author Hardy
 * @since 2023-03-15 13:42:00
 */
@RestController
public class CarRefundOrderController {
    
    /**
     * 服务对象
     */
    @Resource
    private CarRefundOrderService carRefundOrderService;
    
    
}
