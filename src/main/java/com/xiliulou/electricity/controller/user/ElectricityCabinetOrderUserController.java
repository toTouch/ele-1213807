package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 订单表(TElectricityCabinetOrder)表控制层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@RestController
public class ElectricityCabinetOrderUserController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;

    //换电柜下单
    @PostMapping("/user/electricityCabinetOrder/order")
    public R order(@RequestBody ElectricityCabinetOrder electricityCabinetOrder) {

        return electricityCabinetOrderService.order(electricityCabinetOrder);
    }


}