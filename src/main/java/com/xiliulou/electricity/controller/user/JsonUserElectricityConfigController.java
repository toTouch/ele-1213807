package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-06-15:19
 */
public class JsonUserElectricityConfigController {

    @Autowired
    private ElectricityConfigService ElectricityConfigService;

    /**
     * 获取系统设置信息
     */
    @GetMapping("/user/ElectricityConfig/detail")
    public R getElectricityConfig() {
        return R.ok(ElectricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId()));
    }
}
