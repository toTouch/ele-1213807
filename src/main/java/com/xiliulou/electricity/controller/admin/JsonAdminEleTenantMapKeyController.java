package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.query.EleTenantMapKeyAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityConfigAddAndUpdateQuery;
import com.xiliulou.electricity.service.EleTenantMapKeyService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门店表(t_ele_tenant_map_key)表控制层
 *
 * @author makejava
 * @since 2022-08-023 14:59:37
 */
@RestController
public class JsonAdminEleTenantMapKeyController {

    @Autowired
    EleTenantMapKeyService eleTenantMapKeyService;

    //编辑租户高德地图key
    @PutMapping(value = "/admin/eleTenantMapKey")
    @Log(title = "编辑租户高德地图key")
    public R edit(@RequestBody @Validated EleTenantMapKeyAddAndUpdate eleTenantMapKeyAddAndUpdate) {
        return eleTenantMapKeyService.edit(eleTenantMapKeyAddAndUpdate);
    }

    //查询租户高德地图key
    @GetMapping(value = "/admin/eleTenantMapKey")
    public R queryOne() {
        Integer tenantId = TenantContextHolder.getTenantId();
        return R.ok(eleTenantMapKeyService.queryFromCacheByTenantId(tenantId));
    }


}
