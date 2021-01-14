package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;


/**
 * 订单表(TElectricityCabinetOrder)表控制层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@RestController
public class ElectricityCabinetOrderAdminController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;

    //换电柜订单查询
    @GetMapping("/admin/electricityCabinetOrder/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "areaId", required = false) Integer areaId,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "paymentMethod", required = false) Integer paymentMethod) {

        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder()
                .offset(offset)
                .size(size)
                .orderId(orderId)
                .phone(phone)
                .areaId(areaId)
                .status(status)
                .beginTime(beginTime)
                .endTime(endTime)
                .paymentMethod(paymentMethod).build();
        return electricityCabinetOrderService.queryList(electricityCabinetOrderQuery);
    }


    //结束异常订单
    @PutMapping(value = "/admin/electricityCabinetOrder/endOrder")
    public R endOrder(@RequestParam("orderId") String orderId) {
        return electricityCabinetOrderService.endOrder(orderId);
    }


    //换电柜订单导出报表
    @GetMapping("/admin/electricityCabinetOrder/exportExcel")
    public void exportExcel(@RequestParam(value = "orderId", required = false) String orderId,
                            @RequestParam(value = "phone", required = false) String phone,
                            @RequestParam(value = "areaId", required = false) Integer areaId,
                            @RequestParam(value = "status", required = false) Integer status,
                            @RequestParam(value = "beginTime", required = false) Long beginTime,
                            @RequestParam(value = "endTime", required = false) Long endTime, HttpServletResponse response) {

        Double days = (Double.valueOf(endTime - beginTime)) / 1000 / 3600 / 24;
        if (days > 31) {
            return;
        }
        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder()
                .orderId(orderId)
                .phone(phone)
                .areaId(areaId)
                .status(status)
                .beginTime(beginTime)
                .endTime(endTime).build();
        electricityCabinetOrderService.exportExcel(electricityCabinetOrderQuery, response);
    }


}