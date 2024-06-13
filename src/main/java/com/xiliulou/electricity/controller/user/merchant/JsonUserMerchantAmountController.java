package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.merchant.MerchantUserAmountService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author BaoYu
 * @description:
 * @date 2024/3/1 10:57
 */

@Slf4j
@RestController
public class JsonUserMerchantAmountController extends BaseController {
    
    @Resource
    private MerchantUserAmountService merchantUserAmountService;
    
    /**
     * 商户余额
     */
    @GetMapping(value = "/merchant/amount/queryBalance")
    public R queryBalance() {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return R.ok(merchantUserAmountService.queryByUid(user.getUid()));
    }
    
}
