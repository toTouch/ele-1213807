package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.query.AlipayAppConfigQuery;
import com.xiliulou.electricity.service.AlipayAppConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-07-18-15:29
 */
@Slf4j
@RestController
public class JsonAdminAlipayAppConfigController extends BaseController {
    
    @Autowired
    private AlipayAppConfigService alipayAppConfigService;
    
    /**
     * 获取支付宝支付配置列表
     *
     * @return
     */
    @GetMapping(value = "/admin/alipayAppConfig/list")
    public R list() {
        return R.ok(alipayAppConfigService.listByTenantId(TenantContextHolder.getTenantId()));
    }
    
    @PostMapping(value = "/admin/alipayAppConfig")
    @Log(title = "新增支付参数")
    public R save(@RequestBody @Validated(CreateGroup.class) AlipayAppConfigQuery query) {
        return returnTripleResult(alipayAppConfigService.save(query));
    }
    
    @PutMapping(value = "/admin/alipayAppConfig")
    @Log(title = "更新支付参数")
    public R modify(@RequestBody @Validated(UpdateGroup.class) AlipayAppConfigQuery query) {
        return returnTripleResult(alipayAppConfigService.modify(query));
    }
    
    @DeleteMapping(value = "/admin/alipayAppConfig/{id}")
    @Log(title = "删除支付参数")
    public R remove(@PathVariable("id") Long id) {
        return returnTripleResult(alipayAppConfigService.remove(id));
    }
    
}
