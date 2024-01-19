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
     * 电池免押订单V3
     *
     */
    @PostMapping("/user/free/batteryDeposit/v3")
    public R freeBatteryDepositOrder(@RequestBody @Validated FreeBatteryDepositQueryV3 query) {
        return returnTripleResult(freeDepositOrderService.freeBatteryDepositOrderV3(query));
    }

    /**
     * 电池免押混合支付V3
     */
    @PostMapping("/user/freeBatteryDeposit/hybridOrderV3")
    public R freeBatteryDepositHybridOrderV3(@RequestBody @Validated FreeBatteryDepositHybridOrderQuery query, HttpServletRequest request) {
        return returnTripleResult(freeDepositOrderService.freeBatteryDepositHybridOrderV3(query, request));
    }

    /**
     * 查询电池免押是否成功
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
     * 查询车辆免押是否成功
     */
    @GetMapping("/user/free/carDeposit/order/status")
    public R freeCarDepositOrderStatus() {
        return returnTripleResult(freeDepositOrderService.acquireFreeCarDepositStatus());
    }

    /**
     * 电池车辆押金免押的前置检查
     *
     * @return
     */
    @GetMapping("/user/free/carBatteryDeposit/pre/check")
    public R freeCarBatteryDepositPreCheck() {
        return returnTripleResult(freeDepositOrderService.freeCarBatteryDepositPreCheck());
    }

    /**
     * 电池车辆免押订单
     *
     * @return
     */
    @PostMapping("/user/free/carBatteryDeposit/order")
    public R freeCarBatteryDepositOrder(@RequestBody @Validated FreeCarBatteryDepositQuery freeCarBatteryDepositQuery) {
        return returnTripleResult(freeDepositOrderService.freeCarBatteryDepositOrder(freeCarBatteryDepositQuery));
    }

    /**
     * 查询电池车辆免押是否成功（和时孟杨确定改接口已废弃）
     */
    @Deprecated
    @GetMapping("/user/free/carBatteryDeposit/order/status")
    public R freeCarBatteryDepositOrderStatus() {
        return returnTripleResult(freeDepositOrderService.acquireFreeCarBatteryDepositStatus());
    }

    /**
     * 电池免押混合支付
     */
    @PostMapping("/user/freeBatteryDeposit/hybridOrder")
    public R freeBatteryDepositHybridOrder(@RequestBody @Validated FreeBatteryDepositHybridOrderQuery query, HttpServletRequest request) {
        return returnTripleResult(freeDepositOrderService.freeBatteryDepositHybridOrder(query, request));
    }

    /**
     * 车辆免押混合支付
     */
    @PostMapping("/user/freeCarDeposit/hybridOrder")
    public R freeCarDepositHybridOrder(@RequestBody @Validated FreeCarBatteryDepositHybridOrderQuery query, HttpServletRequest request) {
        return returnTripleResult(freeDepositOrderService.freeCarBatteryDepositHybridOrder(query, request));
    }

    /**
     * 电池车辆免押混合支付
     */
    @PostMapping("/user/freeBatteryCarDeposit/hybridOrder")
    public R freeBatteryCarDepositHybridOrder(@RequestBody @Validated FreeCarBatteryDepositOrderQuery query, HttpServletRequest request) {
        return returnTripleResult(freeDepositOrderService.freeCarBatteryCarDepositHybridOrder(query, request));
    }

    /**
     * 获取用户免押订单类型
     *
     * @return
     */
    @GetMapping("/user/freeDepositOrder/detail")
    public R freeDepositOrderDetail() {
        return returnTripleResult(freeDepositOrderService.selectFreeDepositOrderDetail());
    }

}
