package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.CarRefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author zgw
 * @date 2023/3/15 13:50
 * @mood
 */
@RestController
@Slf4j
public class JsonUserCarRefundOrderController {
    
    /**
     * 服务对象
     */
    @Resource
    private CarRefundOrderService carRefundOrderService;
    
    
    @PostMapping("/user/carRefundOrder")
    public R userCarRefundOrder() {
        return carRefundOrderService.userCarRefundOrder();
    }
}
