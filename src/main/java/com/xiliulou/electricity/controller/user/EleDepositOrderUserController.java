package com.xiliulou.electricity.controller.user;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.service.EleDepositOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 缴纳押金订单表(TEleDepositOrder)表控制层
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
@RestController
@Slf4j
public class EleDepositOrderUserController {
    /**
     * 服务对象
     */
    @Autowired
    EleDepositOrderService eleDepositOrderService;

    //缴纳押金
    @PostMapping("/user/payDeposit")
    public R payDeposit(HttpServletRequest request) {
        return eleDepositOrderService.payDeposit(request);
    }

    //退还押金
    @PostMapping("/user/returnDeposit")
    public R returnDeposit(HttpServletRequest request) {
        return eleDepositOrderService.returnDeposit(request); }


    //查询缴纳押金状态
    @PostMapping("/user/eleDepositOrder/queryStatus")
    public R queryStatus(@RequestParam("orderId") String orderId) {
        EleDepositOrder eleDepositOrder=eleDepositOrderService.queryByOrderId(orderId);
        if(Objects.isNull(eleDepositOrder)){
            log.error("ELECTRICITY  ERROR! not found order,orderId{} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        return R.ok(eleDepositOrder.getStatus());
    }

}