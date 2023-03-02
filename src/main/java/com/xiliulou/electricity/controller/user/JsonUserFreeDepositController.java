package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author : eclair
 * @date : 2023/2/15 09:24
 */
@RestController
@Slf4j
public class JsonUserFreeDepositController extends BaseController {

    @Autowired
    FreeDepositOrderService freeDepositOrderService;

    /**
     * 电池押金免押的前置检查
     *
     * @return
     */
    @GetMapping("/user/free/batteryDeposit/pre/check")
    public R freeBatteryDepositPreCheck() {
        return returnTripleResult(freeDepositOrderService.freeBatteryDepositPreCheck());
    }

    /**
     * 电池免押订单
     *
     * @param freeBatteryDepositQuery
     * @return
     */
    @PostMapping("/user/free/batteryDeposit")
    public R freeBatteryDepositOrder(@RequestBody @Validated FreeBatteryDepositQuery freeBatteryDepositQuery) {
        return returnTripleResult(freeDepositOrderService.freeBatteryDepositOrder(freeBatteryDepositQuery));
    }

    /**
     * 查询电池免押订单
     */
    @GetMapping("/user/free/batteryDeposit/order/status")
    public R freeBatteryDepositOrderStatus() {
        return returnTripleResult(freeDepositOrderService.acquireUserFreeBatteryDepositStatus());
    }

    /**
     * 车辆押金免押的前置检查
     *
     * @return
     */
    @GetMapping("/user/free/carDeposit/pre/check")
    public R freeCarDepositPreCheck() {
        return returnTripleResult(freeDepositOrderService.freeCarDepositPreCheck());
    }

    /**
     * 车辆免押订单
     *
     * @return
     */
    @PostMapping("/user/free/carDeposit/order")
    public R freeCarDepositOrder(@RequestBody @Validated FreeCarDepositQuery freeCarDepositQuery) {
        return returnTripleResult(freeDepositOrderService.freeCarDepositOrder(freeCarDepositQuery));
    }

    /**
     * 查询车辆免押订单状态
     */
    @GetMapping("/user/free/carDeposit/order/status")
    public R freeCarDepositOrderStatus() {
        return returnTripleResult(freeDepositOrderService.acquireFreeCarDepositStatus());
    }

    /**
     * 电池免押套餐、保险混合支付
     */
    @PostMapping("/user/freeBatteryDeposit/hybridOrder")
    public R freeBatteryDepositHybridOrder(@RequestBody @Validated FreeBatteryDepositHybridOrderQuery query, HttpServletRequest request) {
        return returnTripleResult(freeDepositOrderService.freeBatteryDepositHybridOrder(query, request));
    }

    /**
     * 车辆免押 套餐混合支付
     */
//    @PostMapping("/user/freeCarDeposit/hybridOrder")
    public R freeCarDepositHybridOrder(@RequestBody @Validated FreeCarDepositHybridOrderQuery query, HttpServletRequest request) {
        return returnTripleResult(freeDepositOrderService.freeCarDepositHybridOrder(query, request));
    }

    /**
     * 车辆&电池免押混合支付
     */
    @PostMapping("/user/freeCarDeposit/hybridOrder")
    public R freeCarBatteryDepositHybridOrder(@RequestBody @Validated FreeCarBatteryDepositHybridOrderQuery query, HttpServletRequest request) {
        return returnTripleResult(freeDepositOrderService.freeCarBatteryDepositHybridOrder(query, request));
    }

}
