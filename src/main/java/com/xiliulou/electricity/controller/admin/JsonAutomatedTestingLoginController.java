/**
 * Create date: 2024/7/4
 */

package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.merchant.AutomatedTestingLoginRequest;
import com.xiliulou.electricity.query.merchant.MerchantLoginRequest;
import com.xiliulou.electricity.service.AutomatedTestingLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/4 17:41
 */
@RestController
public class JsonAutomatedTestingLoginController extends BaseController {

    @Autowired
    private AutomatedTestingLoginService automatedTestingLoginService;
    
    
    
    @PostMapping("/auth/token/automatedTesting")
    public R merchantLogin(HttpServletRequest request, @RequestBody @Validated AutomatedTestingLoginRequest merchantLoginRequest) {
        return returnTripleResult(automatedTestingLoginService.login(request, merchantLoginRequest));
    }
    
    
}
