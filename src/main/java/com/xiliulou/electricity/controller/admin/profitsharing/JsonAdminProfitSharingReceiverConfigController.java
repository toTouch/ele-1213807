/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/22
 */

package com.xiliulou.electricity.controller.admin.profitsharing;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingConfigOptRequest;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingConfigUpdateStatusOptRequest;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverOptConfigRequest;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingConfigService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingReceiverConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingConfigVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/22 16:45
 */
@RestController
public class JsonAdminProfitSharingReceiverConfigController {
    
    
    @Resource
    private ProfitSharingReceiverConfigService profitSharingReceiverConfigService;
    
    
    
    @PostMapping(value = "/admin/profitSharingReceiverConfig")
    public R insert(@Validated @RequestBody ProfitSharingReceiverOptConfigRequest request) {
        request.setTenantId(TenantContextHolder.getTenantId());
        profitSharingReceiverConfigService.insert(request);
        return R.ok();
    }
    
    
    
    
    
}
