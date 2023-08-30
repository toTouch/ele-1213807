package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.query.FranchiseeInsuranceQuery;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-24-13:49
 */
@Slf4j
@RestController
public class JsonUserInsuranceController extends BaseController {

    @Autowired
    private FranchiseeInsuranceService franchiseeInsuranceService;

    /**
     * 根据加盟商、电池型号/车辆型号 查询保险
     *
     * @return
     */
    @GetMapping(value = "/user/selectInsuranceByType")
    public R selectInsuranceByType(@RequestParam("franchiseeId") Long franchiseeId,
                                   @RequestParam("insuranceType") Integer insuranceType,
                                   @RequestParam(value = "storeId", required = false) Long storeId,
                                   @RequestParam(value = "carModelId", required = false) Long carModelId,
                                   @RequestParam(value = "simpleBatteryType", required = false) String simpleBatteryType) {

        FranchiseeInsuranceQuery query = FranchiseeInsuranceQuery.builder()
                .franchiseeId(franchiseeId)
                .insuranceType(insuranceType)
                .storeId(storeId)
                .status(FranchiseeInsurance.STATUS_USABLE)
                .carModelId(carModelId).simpleBatteryType(simpleBatteryType).tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(franchiseeInsuranceService.selectInsuranceByType(query));
    }


}
