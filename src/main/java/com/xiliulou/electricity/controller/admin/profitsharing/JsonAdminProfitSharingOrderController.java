package com.xiliulou.electricity.controller.admin.profitsharing;


import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 分账订单表(profitSharingOrder)表控制层
 *
 * @author maxiaodong
 * @since 2024-08-22 16:58:51
 */
@RestController
@RequestMapping("/admin/profit/sharing/order")
public class JsonAdminProfitSharingOrderController {
    @Resource
    private ProfitSharingOrderService profitSharingOrderService;
    
    
}

