package com.xiliulou.electricity.controller.user.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.query.FranchiseeInsuranceQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseFreeDepositQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePackageOrderQuery;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.enterprise.EnterpriseBatteryPackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/18 15:52
 */

@RestController
@Slf4j
public class JsonUserEnterprisePackageController extends BaseController {
    
    @Resource
    private EnterpriseBatteryPackageService enterpriseBatteryPackageService;
    
    @Resource
    private FranchiseeInsuranceService franchiseeInsuranceService;
    
    @Resource
    private EnterpriseInfoService enterpriseInfoService;
    
    @GetMapping("/user/enterprise/queryBatteryV")
    public R queryBatteryV(@RequestParam(value = "enterpriseId") Long enterpriseId, @RequestParam(value = "uid", required = true) Long uid) {
        
        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder().enterpriseId(enterpriseId).uid(uid).build();
        
        return returnTripleResult(enterpriseBatteryPackageService.queryBatterV(enterpriseChannelUserQuery));
    }
    
    /**
     * 根据电池型号查询可购买套餐信息
     *
     * @param enterpriseId
     * @param uid
     * @param batteryV
     * @return
     */
    @GetMapping("/user/enterprise/queryPackagesByBatteryV")
    public R queryPackagesByBatteryV(@RequestParam(value = "enterpriseId", required = true) Long enterpriseId, @RequestParam(value = "uid", required = true) Long uid,
            @RequestParam(value = "batteryV", required = false) String batteryV) {
        
        EnterpriseMemberCardQuery query = EnterpriseMemberCardQuery.builder().enterpriseId(enterpriseId).uid(uid).batteryV(batteryV).tenantId(TenantContextHolder.getTenantId())
                .build();
        
        return returnTripleResult(enterpriseBatteryPackageService.queryPackagesByBatteryV(query));
    }
    
    /**
     * 根据电池型号查询保险信息
     *
     * @param franchiseeId
     * @param insuranceType     保险类型 0--电池 1--车辆 2--车电一体
     * @param simpleBatteryType
     * @return
     */
    @GetMapping(value = "/user/enterprise/queryInsuranceByType")
    public R queryInsuranceByType(@RequestParam("franchiseeId") Long franchiseeId, @RequestParam("insuranceType") Integer insuranceType,
            @RequestParam(value = "simpleBatteryType", required = false) String simpleBatteryType) {
        
        FranchiseeInsuranceQuery query = FranchiseeInsuranceQuery.builder().franchiseeId(franchiseeId).insuranceType(insuranceType).status(FranchiseeInsurance.STATUS_USABLE)
                .simpleBatteryType(simpleBatteryType).tenantId(TenantContextHolder.getTenantId()).build();
        
        return R.ok(franchiseeInsuranceService.selectInsuranceByType(query));
    }
    
    /**
     * 购买套餐时，查询骑手套餐信息和押金信息
     * @param uid
     * @return
     */
    @GetMapping(value = "/user/enterprise/queryRiderPackageInfo")
    public R queryRiderPackageInfo(@RequestParam(value = "uid", required = true) Long uid) {
        
        return returnTripleResult(enterpriseBatteryPackageService.queryRiderDepositAndPackage(uid));
    
    }
    
    /**
     * 查询指定骑手的押金信息
     * @param uid
     * @return
     */
    @GetMapping(value = "/user/enterprise/queryBatteryDeposit")
    public R queryBatteryDeposit(@RequestParam(value = "uid", required = true) Long uid) {
    
        return returnTripleResult(enterpriseBatteryPackageService.queryUserBatteryDeposit(uid));
    }
    
    /**
     * 生成骑手免押二维码信息
     * @param uid
     * @return
     */
    @GetMapping(value = "/user/enterprise/freeBatteryDeposit")
    public R freeBatteryDeposit(@RequestParam(value = "uid", required = true) Long uid,
                                @RequestParam(value = "realName", required = true) String realName,
                                @RequestParam(value = "idCard", required = true) String idCard,
                                @RequestParam(value = "phone", required = true) String phone,
                                @RequestParam(value = "packageId", required = true) Long packageId) {
    
        EnterpriseFreeDepositQuery freeQuery = EnterpriseFreeDepositQuery.builder()
                .uid(uid)
                .realName(realName)
                .idCard(idCard)
                .phoneNumber(phone)
                .membercardId(packageId)
                .build();
        
        return returnTripleResult(enterpriseBatteryPackageService.freeBatteryDeposit(freeQuery));
    }
    
    
    /**
     * 查询电池免押是否成功
     * @return
     */
    @GetMapping("/user/enterprise/freeDeposit/status")
    public R freeBatteryDepositOrderStatus(@RequestParam(value = "uid", required = true) Long uid) {
        
        return returnTripleResult(enterpriseBatteryPackageService.checkUserFreeBatteryDepositStatus(uid));
    }
    
    /**
     * 根据企业ID查询加盟商信息
     * @param enterpriseId
     * @return
     */
    @GetMapping("/user/enterprise/franchisee/status")
    public R franchiseeStatus(@RequestParam(value = "enterpriseId", required = true) Long enterpriseId) {
        return returnTripleResult(enterpriseBatteryPackageService.selectFranchiseeByEnterpriseId(enterpriseId));
    }
    
    /**
     * 企业代付续租购买套餐
     *
     * @param query
     * @param request
     * @return
     */
    @PostMapping("/user/enterprise/purchaseRenewalPackage")
    public R purchaseRenewalPackage(@RequestBody @Validated(CreateGroup.class) EnterprisePackageOrderQuery query, HttpServletRequest request) {
        
        return returnTripleResult(enterpriseBatteryPackageService.purchasePackageByEnterpriseUser(query, request));
        
    }
    
    /**
     * 企业代付购买套餐+押金+保险
     *
     * @param query
     * @param request
     * @return
     */
    @PostMapping("/user/enterprise/purchasePackageWithDeposit")
    public R purchasePackageWithDeposit(@RequestBody @Validated(CreateGroup.class) EnterprisePackageOrderQuery query, HttpServletRequest request) {
        
        return returnTripleResult(enterpriseBatteryPackageService.purchasePackageWithDepositByEnterpriseUser(query, request));
        
    }
    
    /**
     * 企业免押代付
     * @param query
     * @param request
     * @return
     */
    @PostMapping("/user/enterprise/purchasePackageWithFreeDeposit")
    public R purchasePackageWithFreeDeposit(@RequestBody @Validated(CreateGroup.class) EnterprisePackageOrderQuery query, HttpServletRequest request) {
        
        return returnTripleResult(enterpriseBatteryPackageService.purchasePackageWithFreeDeposit(query, request));
        
    }
    
}
