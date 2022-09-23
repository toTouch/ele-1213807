package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.TenantAddAndUpdateQuery;
import com.xiliulou.electricity.query.TenantQuery;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 租户表(Tenant)表控制层
 *
 * @author makejava
 * @since 2021-06-16 14:31:45
 */
@RestController
public class JsonAdminTenantController {
    /**
     * 服务对象
     */
    @Autowired
    private TenantService tenantService;


    //新增租户
    @PostMapping(value = "/admin/tenant")
    public R addTenant(@Validated(value = CreateGroup.class) @RequestBody TenantAddAndUpdateQuery tenantAddAndUpdateQuery) {

        if (!Objects.equals(TenantContextHolder.getTenantId(), 1)) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return this.tenantService.addTenant(tenantAddAndUpdateQuery);
    }

    //修改租户
    @PutMapping(value = "/admin/tenant")
    public R editTenant(@Validated(value = UpdateGroup.class) @RequestBody TenantAddAndUpdateQuery tenantAddAndUpdateQuery) {

        if (!Objects.equals(TenantContextHolder.getTenantId(), 1)) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return this.tenantService.editTenant(tenantAddAndUpdateQuery);
    }

    //查询租户
    @GetMapping("/admin/tenant/list")
    public R listTenant(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "code", required = false) String code,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime) {

        if (!Objects.equals(TenantContextHolder.getTenantId(), 1)) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TenantQuery tenantQuery = TenantQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .code(code)
                .status(status).build();
        return tenantService.queryListTenant(tenantQuery);
    }

    //查询租户
    @GetMapping("/admin/tenant/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "code", required = false) String code,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime) {

        if (!Objects.equals(TenantContextHolder.getTenantId(), 1)) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        TenantQuery tenantQuery = TenantQuery.builder()
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .code(code)
                .status(status).build();
        return tenantService.queryCount(tenantQuery);
    }

    //
    @GetMapping(value = "/admin/tenant/{id}")
    public R addTenant(@PathVariable("id") Integer id) {
        return R.ok(tenantService.queryByIdFromCache(id));
    }


}
