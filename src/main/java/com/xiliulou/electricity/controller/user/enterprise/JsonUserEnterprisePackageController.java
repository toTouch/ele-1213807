package com.xiliulou.electricity.controller.user.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePackageOrderQuery;
import com.xiliulou.electricity.service.enterprise.EnterpriseBatteryPackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    private EnterpriseInfoService enterpriseInfoService;

    @GetMapping("/user/enterprise/queryBatteryV")
    public R queryBatteryV(@RequestParam(value = "enterpriseId") Long enterpriseId,
                           @RequestParam(value = "uid", required = true) Long uid) {

        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder()
                .enterpriseId(enterpriseId)
                .uid(uid)
                .build();

        return returnTripleResult(enterpriseBatteryPackageService.queryBatterV(enterpriseChannelUserQuery));
    }

    @GetMapping("/user/enterprise/queryPackagesByBatteryV")
    public R queryPackagesByBatteryV(@RequestParam(value = "enterpriseId", required = true) Long enterpriseId,
                                     @RequestParam(value = "uid", required = true) Long uid,
                                     @RequestParam(value = "batteryV", required = false) String batteryV) {


        EnterpriseMemberCardQuery query = EnterpriseMemberCardQuery.builder()
                .enterpriseId(enterpriseId)
                .uid(uid)
                .batteryV(batteryV)
                .tenantId(TenantContextHolder.getTenantId())
                .build();

        return returnTripleResult(enterpriseBatteryPackageService.queryPackagesByBatteryV(query));
    }

    @PostMapping("/user/enterprise/purchasePackage")
    public R purchasePackage(@RequestBody @Validated(CreateGroup.class) EnterprisePackageOrderQuery query, HttpServletRequest request) {

        return returnTripleResult(enterpriseBatteryPackageService.purchasePackageByEnterpriseUser(query, request));

    }

    @GetMapping("/user/enterprise/queryRiderDetails")
    public R queryPackagesByBatteryV(@RequestParam(value = "enterpriseId", required = true) Long enterpriseId,
                                     @RequestParam(value = "uid", required = true) Long uid) {

        EnterpriseMemberCardQuery query = EnterpriseMemberCardQuery.builder()
                .enterpriseId(enterpriseId)
                .uid(uid)
                .build();

        return returnTripleResult(enterpriseBatteryPackageService.queryRiderDetails(query));
    }


}
