package com.xiliulou.electricity.controller.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.MerchantAttrRequest;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: JsonMerchantController
 * @description:
 * @author: renhang
 * @create: 2024-03-26 13:37
 */
@RestController
public class JsonMerchantController extends BaseController {
    
    @Autowired
    private MerchantAttrService merchantAttrService;
    
    
    /**
     * 查询商户升级条件
     */
    @PutMapping("/admin/merchantAttr/upgradeConditionInfo")
    public R upgradeConditionInfo() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return R.ok(merchantAttrService.queryUpgradeCondition(TenantContextHolder.getTenantId()));
    }
    
    /**
     * 修改商户升级条件
     */
    @PutMapping("/admin/merchantAttr/upgradeCondition")
    @Log(title = "修改商户升级条件")
    public R updateUpgradeCondition(@RequestParam("condition") Integer condition) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return returnTripleResult(merchantAttrService.updateUpgradeCondition(TenantContextHolder.getTenantId(), condition));
    }
    
    /**
     * 修改邀请条件
     */
    @PutMapping("/admin/merchantAttr/invitationCondition")
    @Log(title = "修改邀请条件")
    public R updateInvitationCondition(@RequestBody @Validated MerchantAttrRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return returnTripleResult(merchantAttrService.updateInvitationCondition(request));
    }
    
}
