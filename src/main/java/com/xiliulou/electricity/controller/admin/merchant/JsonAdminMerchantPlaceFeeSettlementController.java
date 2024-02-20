package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeSettlementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName : JsonAdminMerchantPlaceFeeSettlementController
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-20
 */

@RestController
public class JsonAdminMerchantPlaceFeeSettlementController extends BaseController {
 
    @Resource
    private MerchantPlaceFeeSettlementService merchantPlaceFeeSettlementService;
    
    
    @PostMapping("/admin/merchant/placeFee/settlement/exportExcel")
    public void export(HttpServletResponse response)
    {
        merchantPlaceFeeSettlementService.export("2024-02",response);
    }

}
