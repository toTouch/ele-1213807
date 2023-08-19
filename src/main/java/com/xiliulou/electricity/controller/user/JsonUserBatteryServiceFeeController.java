package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.BatteryServiceFeeOrderQuery;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeOrderVo;
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

    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;

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
        return R.ok(serviceFeeUserInfoService.queryUserBatteryServiceFee(uid));
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

        BatteryServiceFeeOrderQuery query = BatteryServiceFeeOrderQuery.builder()
                .size(size)
                .offset(offset)
                .tenantId(TenantContextHolder.getTenantId())
                .uid(SecurityUtils.getUid())
                .status(EleBatteryServiceFeeOrderVo.STATUS_SUCCESS)
                .queryStartTime(queryStartTime)
                .queryEndTime(queryEndTime)
                .build();

        return eleBatteryServiceFeeOrderService.queryList(query);
    }


    @GetMapping("/user/batteryServiceFee/info")
    public R selectUserBatteryServiceFee() {
        return R.ok(serviceFeeUserInfoService.selectUserBatteryServiceFee());
    }

}
