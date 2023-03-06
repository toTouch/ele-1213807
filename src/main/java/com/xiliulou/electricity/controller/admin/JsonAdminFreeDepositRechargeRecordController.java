package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.FreeDepositRechargeRecordQuery;
import com.xiliulou.electricity.service.FreeDepositRechargeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 免押次数充值记录
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-20-15:49
 */
@Slf4j
@RestController
public class JsonAdminFreeDepositRechargeRecordController extends BaseController {

    @Autowired
    FreeDepositRechargeRecordService freeDepositRechargeRecordService;

    /**
     * 分页
     */
    @GetMapping("/admin/freeDepositRechargeRecord/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam("tenantId") Integer tenantId,
                  @RequestParam(value = "startTime", required = false) Long startTime,
                  @RequestParam(value = "endTime", required = false) Long endTime) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        FreeDepositRechargeRecordQuery query = FreeDepositRechargeRecordQuery.builder()
                .size(size)
                .offset(offset)
                .tenantId(tenantId)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return R.ok(this.freeDepositRechargeRecordService.selectByPage(query));
    }

    /**
     * 分页总记录数
     */
    @GetMapping("/admin/freeDepositRechargeRecord/queryCount")
    public R pageCount(@RequestParam("tenantId") Integer tenantId,
                       @RequestParam(value = "startTime", required = false) Long startTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        FreeDepositRechargeRecordQuery query = FreeDepositRechargeRecordQuery.builder()
                .tenantId(tenantId)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return R.ok(this.freeDepositRechargeRecordService.selectByPageCount(query));
    }
}
