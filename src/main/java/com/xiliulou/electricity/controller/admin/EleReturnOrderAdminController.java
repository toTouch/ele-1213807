package com.xiliulou.electricity.controller.admin;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.EleRefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * 缴纳押金订单表(TEleDepositOrder)表控制层
 *
 * @author makejava
 * @since 2021-03-02 10:16:44
 */
@RestController
@Slf4j
public class EleReturnOrderAdminController {
    /**
     * 服务对象
     */
    @Autowired
    EleRefundOrderService eleRefundOrderService;

    //后台退款处理
    @PostMapping("/admin/handleRefund")
    public R handleRefund(@RequestParam("refundOrderNo") String refundOrderNo,@RequestParam("status") Integer status,HttpServletRequest request) {
        return eleRefundOrderService.handleRefund(refundOrderNo,status,request);
    }

}