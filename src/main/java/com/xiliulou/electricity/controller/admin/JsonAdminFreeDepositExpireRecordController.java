package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.FreeDepositExpireRecordQuery;
import com.xiliulou.electricity.service.FreeDepositExpireRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @Description:
 * @Author: RenHang
 * @Date: 2025/02/25
 */

@RestController
@RequestMapping("/admin/freeDepositExpireRecord")
public class JsonAdminFreeDepositExpireRecordController {

    @Autowired
    private FreeDepositExpireRecordService freeDepositExpireRecordService;

    /**
     * 分页
     */
    @GetMapping("page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam(value = "status") Integer status,
                  @RequestParam(value = "depositType", required = false) Integer depositType,
                  @RequestParam(value = "uid", required = false) Long uid,
                  @RequestParam(value = "franchiseeId", required = false) Long franchiseeId
    ) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }


        FreeDepositExpireRecordQuery query = FreeDepositExpireRecordQuery.builder()
                .size(size)
                .offset(offset)
                .depositType(depositType)
                .uid(uid)
                .franchiseeId(franchiseeId)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status)
                .build();
        return R.ok(freeDepositExpireRecordService.selectByPage(query));
    }

    /**
     * 分页
     */
    @GetMapping("count")
    public R count(@RequestParam(value = "status") Integer status,
                   @RequestParam(value = "depositType", required = false) Integer depositType,
                   @RequestParam(value = "uid", required = false) Long uid,
                   @RequestParam(value = "franchiseeId", required = false) Long franchiseeId
    ) {
        FreeDepositExpireRecordQuery query = FreeDepositExpireRecordQuery.builder()
                .depositType(depositType)
                .uid(uid)
                .franchiseeId(franchiseeId)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status)
                .build();
        return R.ok(freeDepositExpireRecordService.queryCount(query));
    }


    @GetMapping("offLineDeal")
    public R offLineDeal(@RequestParam(value = "ids") List<Long> ids
    ) {
        freeDepositExpireRecordService.offLineDeal(ids);
        return R.ok();
    }


    @GetMapping("editRemark")
    public R editRemark(@RequestParam(value = "id") Long id, @RequestParam(value = "remark") String remark
    ) {
        freeDepositExpireRecordService.editRemark(id, remark);
        return R.ok();
    }

}
