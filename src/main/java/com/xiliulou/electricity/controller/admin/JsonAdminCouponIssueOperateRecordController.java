package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.CouponIssueOperateRecordQuery;
import com.xiliulou.electricity.service.CouponIssueOperateRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 优惠券发放操作记录表(t_coupon_issue_operate_record)表控制层
 *
 * @author makejava
 * @since 2022-08-19 09:28:22
 */
@RestController
@Slf4j
public class JsonAdminCouponIssueOperateRecordController {


    @Autowired
    CouponIssueOperateRecordService couponIssueOperateRecordService;

    //列表查询
    @GetMapping(value = "/admin/couponIssueOperateRecord/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "couponId", required = false) Integer couponId,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "endTime", required = false) Long issuedUid) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();


        CouponIssueOperateRecordQuery couponIssueOperateRecordQuery = CouponIssueOperateRecordQuery.builder()
                .couponId(couponId)
                .phone(phone)
                .name(name)
                .status(status)
                .beginTime(beginTime)
                .endTime(endTime)
                .offset(offset)
                .size(size)
                .issuedUid(issuedUid)
                .tenantId(tenantId).build();

        return couponIssueOperateRecordService.queryRecordList(couponIssueOperateRecordQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/couponIssueOperateRecord/count")
    public R queryCount(@RequestParam(value = "couponId", required = false) Integer couponId,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();



        CouponIssueOperateRecordQuery couponIssueOperateRecordQuery = CouponIssueOperateRecordQuery.builder()
                .couponId(couponId)
                .phone(phone)
                .name(name)
                .status(status)
                .beginTime(beginTime)
                .endTime(endTime)
                .tenantId(tenantId).build();
        return couponIssueOperateRecordService.queryRecordCount(couponIssueOperateRecordQuery);
    }

}
