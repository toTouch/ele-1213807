package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.request.MutualExchangeAddConfigRequest;
import com.xiliulou.electricity.service.TenantFranchiseeMutualExchangeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 租户下加盟商互通换电controller
 *
 * @author renhang
 * @since 2024-11-27 14:59:37
 */
@RestController
@RequestMapping("/admin/tenant/mutualExchange")
public class JsonAdminTenantFranchiseeMutualExchangeController extends BasicController {
    
    
    @Resource
    private TenantFranchiseeMutualExchangeService tenantFranchiseeMutualExchangeService;
    
    
    @PostMapping("addConfig")
    public R addConfig(@RequestBody @Validated MutualExchangeAddConfigRequest request) {
        return tenantFranchiseeMutualExchangeService.addConfig(request);
    }
}
