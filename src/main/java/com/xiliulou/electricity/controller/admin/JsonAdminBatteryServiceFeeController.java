package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
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
public class JsonAdminBatteryServiceFeeController {
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;
    @Autowired
    EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;


    /**
     * 查询用户的服务费支付记录
     *
     * @param offset
     * @param size
     * @param queryStartTime
     * @param queryEndTime
     * @return
     */
    @GetMapping("/admin/batteryServiceFee/orderList")
    public R queryBatteryServiceFeeOrder(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
                                         @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
                                         @RequestParam(value = "queryEndTime", required = false) Long queryEndTime,
                                         @RequestParam("uid") Long uid,
                                         @RequestParam(value = "status", required = false) Integer status) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return eleBatteryServiceFeeOrderService.queryListForAdmin(offset, size, queryStartTime, queryEndTime, uid, status);
    }

}
