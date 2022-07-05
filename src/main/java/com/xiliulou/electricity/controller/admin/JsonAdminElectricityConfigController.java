package com.xiliulou.electricity.controller.admin;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ElectricityConfigAddAndUpdateQuery;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
public class JsonAdminElectricityConfigController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityConfigService electricityConfigService;

    //编辑平台名称
    @PutMapping(value = "/admin/electricityConfig")
    public R edit(@RequestBody @Validated(value = CreateGroup.class)ElectricityConfigAddAndUpdateQuery electricityConfigAddAndUpdateQuery) {
        return electricityConfigService.edit(electricityConfigAddAndUpdateQuery);
    }

    //查询平台名称
    @GetMapping(value = "/admin/electricityConfig")
    public R queryOne() {
        Integer tenantId = TenantContextHolder.getTenantId();
        return R.ok(electricityConfigService.queryOne(tenantId));
    }

}
