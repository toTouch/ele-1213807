package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.request.merchant.MerchantLevelRequest;
import com.xiliulou.electricity.service.merchant.MerchantLevelService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-04-15:22
 */
@Slf4j
@RestController
public class JsonMerchantLevelController extends BaseController {
    
    @Autowired
    private MerchantLevelService merchantLevelService;
    
    /**
     * 获取商户等级列表
     */
    @GetMapping("/admin/merchantLevel/list")
    public R getMerchantLevel(@RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if(Objects.isNull(franchiseeId)){
            return R.ok(Collections.emptyList());
        }
        
        return R.ok(merchantLevelService.list(TenantContextHolder.getTenantId(), franchiseeId));
    }
    
    /**
     * 修改商户升级内容
     */
    @PutMapping("/admin/merchantLevel/update")
    @Log(title = "修改商户等级配置")
    public R modify(@RequestBody @Validated MerchantLevelRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return returnTripleResult(merchantLevelService.modify(request));
    }
    
    
}
