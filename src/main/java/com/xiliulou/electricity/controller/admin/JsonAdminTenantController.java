package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.TenantQuery;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public R addTenant(@Validated(value = CreateGroup.class) @RequestBody TenantQuery tenantQuery){
        return this.tenantService.addTenant(tenantQuery);
    }

    //修改租户
    @PutMapping(value = "/admin/tenant")
    public R editTenant(@Validated(value = UpdateGroup.class) @RequestBody TenantQuery tenantQuery){
        return this.tenantService.editTenant(tenantQuery);
    }


}
