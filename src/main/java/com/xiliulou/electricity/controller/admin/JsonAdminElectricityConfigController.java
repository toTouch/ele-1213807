package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.query.ElectricityConfigAddAndUpdateQuery;
import com.xiliulou.electricity.query.ElectricityConfigWxCustomerQuery;
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
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
public class JsonAdminElectricityConfigController extends BaseController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityConfigService electricityConfigService;

    //编辑平台名称
    @PutMapping(value = "/admin/electricityConfig")
    @Log(title = "编辑平台信息")
    public R edit(@RequestBody @Validated(value = CreateGroup.class)ElectricityConfigAddAndUpdateQuery electricityConfigAddAndUpdateQuery) {
        return electricityConfigService.edit(electricityConfigAddAndUpdateQuery);
    }

    //查询平台名称
    @GetMapping(value = "/admin/electricityConfig")
    public R queryOne() {
        Integer tenantId = TenantContextHolder.getTenantId();
        return R.ok(electricityConfigService.queryFromCacheByTenantId(tenantId));
    }


    @PutMapping("/admin/electricityConfig/wxCustomer")
    public R editWxCustomer(@RequestBody @Validated ElectricityConfigWxCustomerQuery electricityConfigWxCustomerQuery) {
        return returnTripleResult(electricityConfigService.editWxCustomer(electricityConfigWxCustomerQuery));
    }


}
