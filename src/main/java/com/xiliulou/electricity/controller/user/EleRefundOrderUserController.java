package com.xiliulou.electricity.controller.user;
import com.xiliulou.electricity.service.EleRefundOrderService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 退款订单表(TEleRefundOrder)表控制层
 *
 * @author makejava
 * @since 2021-02-22 10:17:06
 */
@RestController
public class EleRefundOrderUserController {
    /**
     * 服务对象
     */
    @Resource
    private EleRefundOrderService eleRefundOrderService;


}