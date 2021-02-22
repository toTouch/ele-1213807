package com.xiliulou.electricity.controller.user;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.EleDepositOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 缴纳押金订单表(TEleDepositOrder)表控制层
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
@RestController
public class EleDepositOrderUserController {
    /**
     * 服务对象
     */
    @Autowired
    private EleDepositOrderService eleDepositOrderService;

    //缴纳押金
    @PostMapping("/user/payDeposit")
    public R payDeposit(HttpServletRequest request) {

        return eleDepositOrderService.payDeposit(request);

    }

}