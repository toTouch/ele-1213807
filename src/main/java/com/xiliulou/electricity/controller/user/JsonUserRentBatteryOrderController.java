package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.OrderSelfOpenCellQuery;
import com.xiliulou.electricity.query.RentBatteryQuery;
import com.xiliulou.electricity.query.RentOpenDoorQuery;
import com.xiliulou.electricity.request.user.BindBatteryRequest;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.validator.CreateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 租车记录(TRentCarOrder)表控制层
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
@RestController
public class JsonUserRentBatteryOrderController {
    
    /**
     * 服务对象
     */
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    
    @Resource
    private UserInfoService userInfoService;
    
    //租电池
    @PostMapping("/user/rentBattery")
    public R rentBattery(@RequestBody RentBatteryQuery rentBatteryQuery) {
        return rentBatteryOrderService.rentBattery(rentBatteryQuery);
    }
    
    //还电池
    @PostMapping("/user/returnBattery")
    public R returnBattery(@RequestParam("electricityCabinetId") Integer electricityCabinetId) {
        return rentBatteryOrderService.returnBattery(electricityCabinetId);
    }
    
    //再次开门
    @PostMapping("/user/rentBatteryOrder/openDoor")
    public R openDoor(@RequestBody RentOpenDoorQuery rentOpenDoorQuery) {
        return rentBatteryOrderService.openDoor(rentOpenDoorQuery);
    }
    
    
    //查订单状态（新）
    @GetMapping("/user/rentBatteryOrder/queryNewStatus")
    public R queryNewStatus(@RequestParam("orderId") String orderId) {
        return rentBatteryOrderService.queryNewStatus(orderId);
    }
    
    /**
     * 小程序端绑定电池
     *
     * @param bindBatteryRequest request
     * @return R
     */
    @PostMapping("/user/bindBattery")
    public R bindBattery(@RequestBody BindBatteryRequest bindBatteryRequest) {
        return userInfoService.bindBattery(bindBatteryRequest);
    }
    
}
