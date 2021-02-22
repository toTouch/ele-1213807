package com.xiliulou.electricity.controller;

import com.xiliulou.electricity.service.EleDepositOrderService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 缴纳押金订单表(TEleDepositOrder)表控制层
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
@RestController
public class TEleDepositOrderController {
    /**
     * 服务对象
     */
    @Resource
    private EleDepositOrderService tEleDepositOrderService;

}