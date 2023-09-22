package com.xiliulou.electricity.controller.user.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.service.enterprise.EnterpriseBatteryPackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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

    @GetMapping("/user/enterprise/queryBatteryV")
    public R queryBatteryV(@RequestParam(value = "enterpriseId") Long enterpriseId,
                           @RequestParam(value = "uid", required = true) Long uid) {

        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder()
                .enterpriseId(enterpriseId)
                .uid(uid)
                .build();

        return returnTripleResult(enterpriseBatteryPackageService.queryBatterV(enterpriseChannelUserQuery));
    }





}
