package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.merchant.MerchantEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/29 11:45
 */

@RestController
@Slf4j
public class JsonUserMerchantPlaceController {
    
    @Resource
    private MerchantService merchantService;
    
    @Resource
    private MerchantEmployeeService merchantEmployeeService;
    
    @GetMapping("/merchant/place/queryList")
    public R allMerchantEmployeeList(@RequestParam(value = "employeeUid", required = false) Long employeeUid) {
        //商户信息
        Long uid = SecurityUtils.getUid();
        
        return R.ok(merchantService.queryPlaceListByUid(uid, employeeUid));
        
    }
    
}
