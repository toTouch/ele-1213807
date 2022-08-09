package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UpgradeNotifyMailQuery;
import com.xiliulou.electricity.service.TenantNotifyMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统升级通知邮箱
 *
 * @author zzlong
 * @since 2022-08-08 15:30:14
 */
@RestController
@RequestMapping("/admin/tenantNotifyMail/")
public class JsonAdminTenantNotifyMailController {

    @Autowired
    private TenantNotifyMailService tenantNotifyMailService;

    @GetMapping("select")
    public R selectOne() {
        return R.ok(this.tenantNotifyMailService.selectByTenantId());
    }

    @PostMapping("insert")
    public R insert(@Validated UpgradeNotifyMailQuery upgradeNotifyMailQuery) {
        return this.tenantNotifyMailService.insert(upgradeNotifyMailQuery);
    }

    /**
     * 检查当前租户是否绑定通知邮箱
     *
     * @return
     */
    @GetMapping("check")
    public R check() {
        return R.ok(this.tenantNotifyMailService.checkByTenantId());
    }

}
