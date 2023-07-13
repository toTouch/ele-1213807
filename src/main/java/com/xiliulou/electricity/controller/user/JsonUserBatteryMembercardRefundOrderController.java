package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-12-15:48
 */
@Slf4j
@RestController
public class JsonUserBatteryMembercardRefundOrderController extends BaseController {

    @Autowired
    private BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;

    /**
     * 退租申请
     */
    @PostMapping("/user/batteryMembercardRefund/{orderNo}")
    public R batteryMembercardRefund(String orderNo) {
        return returnTripleResult(batteryMembercardRefundOrderService.batteryMembercardRefund(orderNo));
    }

    /**
     * 可退租订单详情
     */
    @GetMapping("/user/batteryMembercardRefund/{orderNo}")
    public R batteryMembercardRefundOrderDetail(String orderNo) {
        return returnTripleResult(batteryMembercardRefundOrderService.batteryMembercardRefundOrderDetail(orderNo));
    }


}
