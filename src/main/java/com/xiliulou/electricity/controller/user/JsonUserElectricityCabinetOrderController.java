package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.query.OpenDoorQuery;
import com.xiliulou.electricity.query.OrderQuery;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;


/**
 * 订单表(TElectricityCabinetOrder)表控制层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@RestController
@Slf4j
public class JsonUserElectricityCabinetOrderController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;

    //换电柜下单
    @PostMapping("/user/electricityCabinetOrder/order")
    public R order(@RequestBody @Validated(value = CreateGroup.class)  OrderQuery orderQuery) {
        return electricityCabinetOrderService.order(orderQuery);
    }

    //换电柜再次开门
    @PostMapping("/user/electricityCabinetOrder/openDoor")
    public R openDoor(@RequestBody OpenDoorQuery openDoorQuery) {
        return electricityCabinetOrderService.openDoor(openDoorQuery);
    }

    //换电柜订单查询
    @GetMapping("/user/electricityCabinetOrder/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {

        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder()
                .offset(offset)
                .size(size)
                .beginTime(beginTime)
                .endTime(endTime)
                .uid(user.getUid()).build();
        return electricityCabinetOrderService.queryList(electricityCabinetOrderQuery);
    }

    //换电柜订单量
    @GetMapping("/user/electricityCabinetOrder/count")
    public R queryCount(@RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder()
                .beginTime(beginTime)
                .endTime(endTime)
                .uid(user.getUid()).build();
        return electricityCabinetOrderService.queryCount(electricityCabinetOrderQuery);
    }

    //查订单状态
    @GetMapping("/user/electricityCabinetOrder/queryStatus")
    public R queryStatus(@RequestParam("orderId") String orderId) {
        return electricityCabinetOrderService.queryStatus(orderId);
    }


    //查订单状态（新）
    @GetMapping("/user/electricityCabinetOrder/queryNewStatus")
    public R queryNewStatus(@RequestParam("orderId") String orderId) {
        return electricityCabinetOrderService.queryNewStatus(orderId);
    }


}
