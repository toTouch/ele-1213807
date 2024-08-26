package com.xiliulou.electricity.controller.admin.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 16:49
 */
@RestController
@Slf4j
@RequestMapping("/admin/installment/deductionPlan")
public class JsonAdminInstallmentDeductionPlanController {
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private UserDataScopeService userDataScopeService;
    
    @Autowired
    private InstallmentDeductionPlanService installmentDeductionPlanService;
    
    @GetMapping("/listDeductionPlanForRecord")
    public R<List<InstallmentDeductionPlan>> listDeductionPlanByAgreementNo(@RequestParam(value = "externalAgreementNo") String externalAgreementNo) {
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok();
        }
        // 用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(storeIds)) {
                return R.ok();
            }
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        }
        
        InstallmentRecordQuery installmentRecordQuery = InstallmentRecordQuery.builder().tenantId(tenantId).franchiseeIds(franchiseeIds).storeIds(storeIds)
                .externalAgreementNo(externalAgreementNo).build();
        
        return installmentDeductionPlanService.listDeductionPlanByAgreementNo(installmentRecordQuery);
    }
}
