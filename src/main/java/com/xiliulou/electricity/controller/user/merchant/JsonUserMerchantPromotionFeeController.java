package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.service.merchant.MerchantPromotionFeeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName : JsonUserMerchantPromotionFee
 * @Description : 小程序推广费
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
@RestController
@Slf4j
public class JsonUserMerchantPromotionFeeController {
    
    @Resource
    private MerchantPromotionFeeService merchantPromotionFeeService;
    
    @GetMapping("/user/merchant/promotionFee/availableWithdrawAmount")
    public R queryMerchantAvailableWithdrawAmount() {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return merchantPromotionFeeService.queryMerchantAvailableWithdrawAmount(user.getUid());
    }
    
    @GetMapping("/user/merchant/promotionFee/income")
    public R queryMerchantPromotionFeeIncome(@RequestParam("type") Integer type, @RequestParam("uid") Long uid) {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return merchantPromotionFeeService.queryMerchantPromotionFeeIncome(type, uid);
    }
    
    @GetMapping("/user/merchant/promotionFee/scanCodeCount")
    public R queryMerchantPromotionScanCode(@RequestParam("type") Integer type, @RequestParam("uid") Long uid) {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return merchantPromotionFeeService.queryMerchantPromotionScanCode(type, uid);
    }
    
}
