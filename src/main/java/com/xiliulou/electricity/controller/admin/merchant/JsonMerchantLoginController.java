package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.merchant.MerchantLoginRequest;
import com.xiliulou.electricity.service.merchant.MerchantTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : eclair
 * @date : 2024/2/18 10:11
 */
@RestController
@Slf4j
public class JsonMerchantLoginController extends BaseController {

    @Autowired
    MerchantTokenService tokenService;

    @PostMapping("/auth/token/merchant")
    public R merchantLogin(@RequestBody @Validated MerchantLoginRequest merchantLoginRequest) {
        return returnTripleResult(tokenService.login(merchantLoginRequest));
    }
}
