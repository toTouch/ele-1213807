package com.xiliulou.electricity.controller.user.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.query.enterprise.EnterpriseInfoQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePurchaseOrderQuery;
import com.xiliulou.electricity.query.enterprise.UserCloudBeanRechargeQuery;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;


/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-14-16:02
 */
@RestController
@Slf4j
public class JsonUserEnterpriseInfoController extends BaseController {
    
    @Resource
    private EnterpriseInfoService enterpriseInfoService;
    
    /**
     * 获取用户是否属于企业渠道
     */
    @GetMapping("/user/enterpriseInfo/check")
    public R enterpriseInfoCheck() {
        return R.ok(enterpriseInfoService.checkUserType());
    }
    
    /**
     * 获取用户云豆详情
     */
    @GetMapping("/user/cloudBean/detail")
    public R cloudBeanDetail() {
        return R.ok(enterpriseInfoService.cloudBeanDetail());
    }
    
    /**
     * 根据UID查询企业详情
     *
     * @return
     */
    @GetMapping("/user/enterpriseInfo/detail")
    public R queryEnterpriseInfo() {
        return R.ok(enterpriseInfoService.selectDetailByUid(SecurityUtils.getUid()));
    }
    
    /**
     * 云豆充值
     */
    @PutMapping("/user/enterpriseInfo/recharge")
    public R recharge(@RequestBody @Validated UserCloudBeanRechargeQuery userCloudBeanRechargeQuery, HttpServletRequest request) {
        return returnTripleResult(enterpriseInfoService.rechargeForUser(userCloudBeanRechargeQuery, request));
    }
    
    /**
     * 云豆概览
     */
    @GetMapping("/user/enterpriseInfo/cloudBean/generalView")
    public R cloudBeanGeneralView() {
        return returnTripleResult(enterpriseInfoService.cloudBeanGeneralView());
    }
    
    /**
     * 云豆回收
     */
    @PutMapping("/user/enterpriseInfo/recycleCloudBean/{uid}")
    public R recycleCloudBean(@PathVariable("uid") Long uid) {
        return returnTripleResult(enterpriseInfoService.recycleCloudBean(uid));
    }
    
    /**
     * 骑手概览
     *
     * @return
     */
    @GetMapping("/user/enterpriseInfo/queryPurchasePackageCount")
    public R queryPurchasePackageCount() {
        Long uid = SecurityUtils.getUid();
        Long tenantId = TenantContextHolder.getTenantId().longValue();
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.selectByUid(uid);
        
        if (Objects.isNull(enterpriseInfo)) {
            return R.fail("300074", "未找到企业信息");
        }
        
        EnterprisePurchaseOrderQuery query = EnterprisePurchaseOrderQuery.builder().enterpriseId(enterpriseInfo.getId()).tenantId(tenantId).build();
        
        return R.ok(enterpriseInfoService.queryPurchasedPackageCount(query));
    }
    
    /**
     * 企业端更新骑手自主续费状态，总开关
     *
     * @return
     */
    @PutMapping("/user/enterpriseInfo/updateAllRenewalStatus/{renewalStatus}")
    public R updateAllRenewalStatus(@PathVariable("renewalStatus") Integer renewalStatus) {
        Integer tenantId = TenantContextHolder.getTenantId();
        EnterpriseInfoQuery enterpriseInfoQuery = EnterpriseInfoQuery.builder().renewalStatus(renewalStatus).tenantId(tenantId).build();
        
        return R.ok(enterpriseInfoService.updateAllRenewalStatus(enterpriseInfoQuery));
    }
    
}
