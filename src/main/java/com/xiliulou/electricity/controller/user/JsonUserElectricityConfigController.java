package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-06-15:19
 */
@RestController
public class JsonUserElectricityConfigController {
    
    @Autowired
    private ElectricityConfigService ElectricityConfigService;
    
    /**
     * 获取系统设置信息
     */
    @GetMapping("/user/electricityConfig/detail")
    public R getElectricityConfig() {
        return R.ok(ElectricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId()));
    }
}
