package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/24 17:30
 */

@Slf4j
@RestController
public class JsonUserMerchantWithdrawController extends BaseController {
    
    @Resource
    MerchantWithdrawApplicationService merchantWithdrawApplicationService;
    
    @PostMapping("/merchant/withdraw/application")
    public R withdrawApplication(@Validated @RequestBody MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return  R.fail("120013", "未找到用户");
        }
        merchantWithdrawApplicationRequest.setUid(user.getUid());
        merchantWithdrawApplicationRequest.setTenantId(tenantId);
        
        return returnTripleResult(merchantWithdrawApplicationService.saveMerchantWithdrawApplication(merchantWithdrawApplicationRequest));
    }
    
    @GetMapping("/merchant/withdrawList")
    public R queryMerchantWithdrawApplicationList(@RequestParam(value = "size", required = true) Long size,
            @RequestParam(value = "offset", required = true) Long offset,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status) {
    
        if (size < 0 || size > 50) {
            size = 10L;
        }
    
        if (offset < 0) {
            offset = 0L;
        }
    
        Integer tenantId = TenantContextHolder.getTenantId();
    
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("bindBank  ERROR! not found user ");
            return R.fail("", "没有查询到相关用户");
        }
    
        MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest = MerchantWithdrawApplicationRequest.builder()
                .size(size)
                .offset(offset)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .uid(uid)
                .tenantId(tenantId)
                .build();
        
        return R.ok(merchantWithdrawApplicationService.queryMerchantWithdrawApplicationList(merchantWithdrawApplicationRequest));
    }
    
    

}
