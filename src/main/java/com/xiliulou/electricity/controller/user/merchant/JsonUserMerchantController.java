package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.merchant.MerchantVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

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
     * 获取商户/渠道员详情
     *
     * @return
     */
    @GetMapping("/merchant/queryMerchantDetail")
    public R queryMerchantDetail() {
        return R.ok(merchantService.queryMerchantUserDetail());
    }
    
    /**
     * 获取商户详情
     *
     * @return
     */
    @GetMapping("/merchant/getMerchantQrCode")
    public R getMerchantQrCode() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Merchant merchant = merchantService.queryByUid(user.getUid());
        if (Objects.isNull(merchant)) {
            log.error("merchant get merchant qr code merchant is null, uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return R.ok(merchantService.getMerchantQrCode(user.getUid(), merchant.getId()));
    }
    
    /**
     * 获取商户详情
     *
     * @return
     */
    @GetMapping("/merchant/getMerchantInfo")
    public R getMerchantInfo() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Merchant merchant = merchantService.queryByUid(uid);
        if (Objects.isNull(merchant)) {
            log.error("merchant get merchant qr code merchant is null, uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantVO merchantVO = MerchantVO.builder().enterprisePackageAuth(merchant.getEnterprisePackageAuth()).inviteAuth(merchant.getInviteAuth()).build();
        
        return R.ok(merchantVO);
    }
    
}
