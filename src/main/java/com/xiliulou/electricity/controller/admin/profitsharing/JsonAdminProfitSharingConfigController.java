/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/22
 */

package com.xiliulou.electricity.controller.admin.profitsharing;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.admin.base.AbstractFranchiseeDataPermissionController;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingConfigOptRequest;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingConfigUpdateStatusOptRequest;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingConfigRemainingVO;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingConfigVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
public class JsonAdminProfitSharingConfigController extends AbstractFranchiseeDataPermissionController {
    
    
    @Resource
    private ProfitSharingConfigService profitSharingConfigService;
    
    @PutMapping(value = "/admin/profitSharingConfig")
    public R update(@Validated @RequestBody ProfitSharingConfigOptRequest request) {
        request.setTenantId(TenantContextHolder.getTenantId());
        // 校验加盟商权限
        this.checkFranchiseeDataPermission();
        
        profitSharingConfigService.update(request);
        return R.ok();
    }
    
    /**
     * 根据加盟商查询分账方配置
     *
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/8/22 19:07
     */
    @GetMapping(value = "/admin/profitSharingConfig/{franchiseeId}")
    public R getByFranchiseeId(@PathVariable("franchiseeId") Long franchiseeId) {
        // 校验加盟商权限
        this.checkFranchiseeDataPermissionByFranchiseeId(franchiseeId);
        
        ProfitSharingConfigVO profitSharingConfigVO = profitSharingConfigService.queryByTenantIdAndFranchiseeId(TenantContextHolder.getTenantId(), franchiseeId);
        return R.ok(profitSharingConfigVO);
    }
    
    /**
     * 开关状态更新
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/23 15:35
     */
    @PutMapping(value = "/admin/profitSharingConfig/updateStatus")
    public R updateStatus(@Validated @RequestBody ProfitSharingConfigUpdateStatusOptRequest request) {
        
        // 校验加盟商权限
        this.checkFranchiseeDataPermissionByFranchiseeId(request.getFranchiseeId());
        
        request.setTenantId(TenantContextHolder.getTenantId());
        profitSharingConfigService.updateStatus(request);
        return R.ok();
    }
    
    
    @GetMapping(value = "/admin/profitSharingConfig/getRemainingScaleLimit/{id}")
    public R getRemainingScaleLimit(@PathVariable("id") Long id) {
        
        ProfitSharingConfigRemainingVO profitSharingConfigVO = profitSharingConfigService.queryRemainingScaleLimit(TenantContextHolder.getTenantId(), id);
        
        this.checkFranchiseeDataPermissionByFranchiseeId(profitSharingConfigVO.getFranchiseeId());
        
        return R.ok(profitSharingConfigVO);
    }
    
    
}
