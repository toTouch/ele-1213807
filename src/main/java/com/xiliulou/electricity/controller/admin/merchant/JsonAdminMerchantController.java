package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.MerchantAttrRequest;
import com.xiliulou.electricity.request.merchant.MerchantSaveRequest;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 10:10
 */
@Slf4j
@RestController
public class JsonAdminMerchantController extends BaseController {
    
    @Autowired
    private MerchantAttrService merchantAttrService;
    
    @Resource
    private MerchantService merchantService;
    
    /**
     * 修改商户升级条件
     */
    @PutMapping("/admin/merchantAttr/upgradeCondition")
    public R updateUpgradeCondition(@RequestParam("merchantId") Long merchantId, @RequestParam("condition") Integer condition) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return returnTripleResult(merchantAttrService.updateUpgradeCondition(merchantId, condition));
    }
    
    /**
     * 修改邀请条件
     */
    @PutMapping("/admin/merchantAttr/invitationCondition")
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
    
    /**
     * 保存
     *
     * @param failureAlarmSaveRequest
     * @return
     */
    @PostMapping("/admin/merchant/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) MerchantSaveRequest merchantSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Triple<Boolean, String, Object> r = merchantService.save(merchantSaveRequest, user.getUid());
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
        
        return R.ok();
    }
    
}
