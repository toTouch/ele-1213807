package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.merchant.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 10:11
 */
@Slf4j
@RestController
public class JsonUserMerchantController extends BaseController {
    
    @Autowired
    private MerchantService merchantService;
    
    /**
     * 获取商户详情
     * @return
     */
    @GetMapping("/user/merchant/queryMerchantDetail")
    public R queryMerchantDetail() {
        return R.ok(merchantService.queryMerchantDetail());
    }
    
    
}
