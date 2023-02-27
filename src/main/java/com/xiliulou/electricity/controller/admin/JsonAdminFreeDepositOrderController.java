package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.FreeDepositOrderQuery;
import com.xiliulou.electricity.query.FreeDepositRechargeRecordQuery;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-24-9:53
 */
@RestController
@Slf4j
public class JsonAdminFreeDepositOrderController extends BaseController {

    @Autowired
    private FreeDepositOrderService freeDepositOrderService;

    /**
     * 分页
     */
    @GetMapping("/admin/freeDepositOrder/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam(value = "authStatus", required = false) Integer authStatus,
                  @RequestParam(value = "payStatus", required = false) Integer payStatus,
                  @RequestParam(value = "depositType", required = false) Integer depositType,
                  @RequestParam(value = "orderId", required = false) String orderId,
                  @RequestParam(value = "phone", required = false) String phone,
                  @RequestParam(value = "realName", required = false) String realName,
                  @RequestParam(value = "startTime", required = false) Long startTime,
                  @RequestParam(value = "endTime", required = false) Long endTime) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        FreeDepositOrderQuery query = FreeDepositOrderQuery.builder()
                .size(size)
                .offset(offset)
                .authStatus(authStatus)
                .payStatus(payStatus)
                .depositType(depositType)
                .orderId(orderId)
                .phone(phone)
                .realName(realName)
                .tenantId(TenantContextHolder.getTenantId())
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return R.ok(this.freeDepositOrderService.selectByPage(query));
    }

    /**
     * 分页总记录数
     */
    @GetMapping("/admin/freeDepositOrder/queryCount")
    public R pageCount(@RequestParam(value = "authStatus", required = false) Integer authStatus,
                       @RequestParam(value = "payStatus", required = false) Integer payStatus,
                       @RequestParam(value = "depositType", required = false) Integer depositType,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "realName", required = false) String realName,
                       @RequestParam(value = "startTime", required = false) Long startTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        FreeDepositOrderQuery query = FreeDepositOrderQuery.builder()
                .authStatus(authStatus)
                .payStatus(payStatus)
                .depositType(depositType)
                .orderId(orderId)
                .phone(phone)
                .realName(realName)
                .tenantId(TenantContextHolder.getTenantId())
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return R.ok(this.freeDepositOrderService.selectByPageCount(query));
    }


}
