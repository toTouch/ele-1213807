package com.xiliulou.electricity.controller.admin;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.service.EleRefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;


/**
 * 缴纳押金订单表(TEleDepositOrder)表控制层
 *
 * @author makejava
 * @since 2021-03-02 10:16:44
 */
@RestController
@Slf4j
public class EleRefundOrderAdminController {
    /**
     * 服务对象
     */
    @Autowired
    EleRefundOrderService eleRefundOrderService;

    //退款列表
    @GetMapping("/admin/eleRefundOrder/queryList")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "refundOrderNo", required = false) String refundOrderNo,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {

        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        EleRefundQuery eleRefundQuery = EleRefundQuery.builder()
                .offset(offset)
                .size(size)
                .orderId(orderId)
                .status(status)
                .beginTime(beginTime)
                .endTime(endTime).build();

        return eleRefundOrderService.queryList(eleRefundQuery);
    }

    //后台退款处理
    @PostMapping("/admin/handleRefund")
    public R handleRefund(@RequestParam("refundOrderNo") String refundOrderNo,@RequestParam("status") Integer status,HttpServletRequest request) {
        return eleRefundOrderService.handleRefund(refundOrderNo,status,request);
    }

}