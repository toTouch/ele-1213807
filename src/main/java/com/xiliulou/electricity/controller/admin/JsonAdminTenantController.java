package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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


}
