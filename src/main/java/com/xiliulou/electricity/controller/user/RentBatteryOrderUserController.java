package com.xiliulou.electricity.controller.user;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.RentBatteryQuery;
import com.xiliulou.electricity.query.RentOpenDoorQuery;
import com.xiliulou.electricity.query.ReturnBatteryQuery;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
/**
 * 租车记录(TRentCarOrder)表控制层
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
@RestController
public class RentBatteryOrderUserController {
    /**
     * 服务对象
     */
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;

    //租电池
    @PostMapping("/user/rentBattery")
    public R rentBattery(@RequestBody RentBatteryQuery rentBatteryQuery) {
        return rentBatteryOrderService.rentBattery(rentBatteryQuery);
    }

    //还电池
    @PostMapping("/user/returnBattery")
    public R returnBattery(@RequestBody ReturnBatteryQuery returnBatteryQuery) {
        return rentBatteryOrderService.returnBattery(returnBatteryQuery);
    }

    //再次开门
    @PostMapping("/user/rentBatteryOrder/openDoor")
    public R openDoor(@RequestBody RentOpenDoorQuery rentOpenDoorQuery) {
        return rentBatteryOrderService.openDoor(rentOpenDoorQuery);
    }

    //查订单状态
    @GetMapping("/user/rentBatteryOrder/queryStatus")
    public R queryStatus(@RequestParam("orderId") String orderId) {
        return rentBatteryOrderService.queryStatus(orderId);
    }

}