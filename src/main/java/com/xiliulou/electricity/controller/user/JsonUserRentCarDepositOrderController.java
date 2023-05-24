package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.CarDepositOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-22-10:32
 */
@RestController
@Slf4j
public class JsonUserRentCarDepositOrderController extends BaseController {
    @Autowired
    private CarDepositOrderService carDepositOrderService;

    /**
     * 缴纳租车押金
     *
     * @return
     */
    @PostMapping("/user/rentCarDeposit/order")
    public R payRentCarDeposit(@RequestParam(value = "storeId") Long storeId,
                               @RequestParam(value = "carModelId") Integer carModelId,
                               HttpServletRequest request) {
        return returnTripleResult(carDepositOrderService.payRentCarDeposit(storeId, carModelId, request));
    }

    //用户查询租车押金
    @GetMapping(value = "/user/rentCarDeposit/detail")
    public R queryRentCarDeposit() {
        return returnTripleResult(carDepositOrderService.selectRentCarDeposit());
    }

    /**
     * 退租车押金
     *
     * @return
     */
    @PostMapping("/user/rentCarDeposit/refund")
    @Deprecated
    public R refundRentCarDeposit(HttpServletRequest request) {
        return returnTripleResult(carDepositOrderService.refundRentCarDeposit(request));
    }


}
