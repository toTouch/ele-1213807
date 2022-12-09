package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 电池服务费控制层
 *
 * @author makejava
 * @since 2022-04-21 09:44:36
 */
@RestController
@Slf4j
public class JsonUserBatteryServiceFeeController {
    @Autowired
    ElectricityBatteryService electricityBatteryService;

    @Autowired
    EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;

    /**
     * 查询电池服务费
     *
     * @return
     */
    @GetMapping("/user/batteryServiceFee/query")
    public R queryBatteryServiceFee() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }
        return R.ok(franchiseeUserInfoService.queryUserBatteryServiceFee(uid));
    }

    /**
     * 查询用户的服务费支付记录
     *
     * @param offset
     * @param size
     * @param queryStartTime
     * @param queryEndTime
     * @return
     */
    @GetMapping("/user/batteryServiceFee/orderList")
    public R queryBatteryServiceFeeOrder(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
                                         @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
                                         @RequestParam(value = "queryEndTime", required = false) Long queryEndTime) {
        return eleBatteryServiceFeeOrderService.queryList(offset, size, queryStartTime, queryEndTime);
    }

}
