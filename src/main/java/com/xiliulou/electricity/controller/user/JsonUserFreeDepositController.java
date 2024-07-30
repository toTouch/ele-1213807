package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.ttl.ChannelSourceContextHolder;
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
        query.setPaymentChannel(ChannelSourceContextHolder.get());
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
     * 电池车辆押金免押的前置检查
     *
     * @return
     */
    @GetMapping("/user/free/carBatteryDeposit/pre/check")
    public R freeCarBatteryDepositPreCheck() {
        return returnTripleResult(freeDepositOrderService.freeCarBatteryDepositPreCheck());
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
