package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.query.OpenDoorQuery;
import com.xiliulou.electricity.query.OrderQuery;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;


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
    public R order(@RequestBody OrderQuery orderQuery) {
        return electricityCabinetOrderService.order(orderQuery);
    }

    //换电柜再次开门
    @PostMapping("/user/electricityCabinetOrder/openDoor")
    public R openDoor(@RequestBody OpenDoorQuery openDoorQuery) {
        return electricityCabinetOrderService.openDoor(openDoorQuery);
    }

    //换电柜订单查询
    @PostMapping("/user/electricityCabinetOrder/list")
    public R queryList(@RequestParam(value = "size", required = false) Integer size,
                       @RequestParam(value = "offset", required = false) Integer offset,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {

        if (Objects.isNull(size)) {
            size = 10;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0;
        }

        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder()
                .offset(offset)
                .size(size)
                .beginTime(beginTime)
                .endTime(endTime).build();
        return electricityCabinetOrderService.queryList(electricityCabinetOrderQuery);
    }

    //换电柜订单量
    @PostMapping("/user/electricityCabinetOrder/count")
    public R queryCount(@RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {


        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder()
                .beginTime(beginTime)
                .endTime(endTime).build();
        return electricityCabinetOrderService.queryCount(electricityCabinetOrderQuery);
    }


}