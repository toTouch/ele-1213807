package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.MerchantModifyInviterRequest;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.UpdateGroup;
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
 * @author HeYafeng
 * @description 用户管理
 * @date 2024/2/6 13:37:49
 */
@Slf4j
@RestController
public class JsonMerchantUserInfoExtraController extends BaseController {
    
    @Resource
    private UserInfoExtraService userInfoExtraService;
    
    /**
     * 返回null-不显示“修改邀请人”按钮
     */
    @GetMapping(value = "/admin/merchant/userInfoExtra/modifyInviterInfo")
    public R modifyInviterInfo(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam("uid") Long uid) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return R.ok(userInfoExtraService.selectModifyInviterInfo(uid, size, offset));
    }
    
    /**
     * 修改邀请人
     */
    @PostMapping(value = "/admin/merchant/userInfoExtra/modifyInviter")
    public R modifyInviter(@RequestBody @Validated(UpdateGroup.class) MerchantModifyInviterRequest merchantModifyInviterRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return userInfoExtraService.modifyInviter(merchantModifyInviterRequest);
    }
    
}
