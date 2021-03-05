package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 租车记录(TRentCarOrder)表控制层
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
@RestController
public class RentBatteryOrderAdminController {
    /**
     * 服务对象
     */
    @Autowired
    private RentBatteryOrderService rentBatteryOrderService;

    //列表查询
    @GetMapping(value = "/admin/rentBatteryOrder/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "batteryStoreId", required = false) Integer batteryStoreId,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "userName", required = false) String userName,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "orderId", required = false) String orderId) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder()
                .offset(offset)
                .size(size)
                .userName(userName)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .orderId(orderId).build();

        return rentBatteryOrderService.queryList(rentBatteryOrderQuery);
    }


    //结束异常订单
    @PostMapping(value = "/admin/rentBatteryOrder/endOrder")
    public R endOrder(@RequestParam("orderId") String orderId) {
        return rentBatteryOrderService.endOrder(orderId);
    }

}