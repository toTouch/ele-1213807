package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.TenantAppInfoService;
import com.xiliulou.electricity.web.query.AppInfoQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author : eclair
 * @date : 2021/7/21 9:58 上午
 */
@RestController
@Slf4j
public class JsonAdminTenantAppInfoController extends BaseController {
    @Autowired
    TenantAppInfoService tenantAppInfoService;

    @PostMapping("/admin/app/save")
    public R saveApp(@RequestBody @Validated AppInfoQuery appInfoQuery) {
        return returnTripleResult(tenantAppInfoService.saveApp(appInfoQuery));
    }


    @GetMapping("/admin/app/info")
    public R getAppInfo(@RequestParam("appType") String appType) {
        return returnTripleResult(tenantAppInfoService.queryAppInfo(appType));
    }
}
