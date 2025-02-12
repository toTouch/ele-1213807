package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.CouponPackageOperateRecordQuery;
import com.xiliulou.electricity.service.CouponPackageOperateRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * @Description: JsonAdminCouponPackageOperateRecordController
 * @Author: renhang
 * @Date: 2025/02/05
 */

@RestController
@Slf4j
public class JsonAdminCouponPackageOperateRecordController {


    @Resource
    CouponPackageOperateRecordService couponPackageOperateRecordService;


    @GetMapping(value = "/admin/couponPackageOperateRecord/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "packageId", required = false) Integer packageId,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();


        CouponPackageOperateRecordQuery couponPackageOperateRecordQuery = CouponPackageOperateRecordQuery.builder()
                .packageId(packageId)
                .phone(phone)
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .offset(offset)
                .size(size)
                .tenantId(tenantId).build();
        return couponPackageOperateRecordService.queryRecordList(couponPackageOperateRecordQuery);
    }


    @GetMapping(value = "/admin/couponPackageOperateRecord/count")
    public R queryCount(@RequestParam(value = "packageId", required = false) Integer packageId,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();


        CouponPackageOperateRecordQuery couponPackageOperateRecordQuery = CouponPackageOperateRecordQuery.builder()
                .packageId(packageId)
                .phone(phone)
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .tenantId(tenantId).build();
        return couponPackageOperateRecordService.queryRecordCount(couponPackageOperateRecordQuery);
    }

}
