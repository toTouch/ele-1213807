package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.MerchantModifyInviterRequest;
import com.xiliulou.electricity.request.merchant.MerchantModifyInviterUpdateRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
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
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    /**
     * 修改邀请人 邀请人（商户）列表
     */
    @GetMapping(value = "/admin/merchant/userInfoExtra/selectInviterList")
    public R modifyInviterInfo(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam("uid") Long uid,
            @RequestParam(value = "name", required = false) String name) {
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.emptyList());
            }
        }
        
        MerchantModifyInviterRequest request = MerchantModifyInviterRequest.builder().size(size).offset(offset).uid(uid).merchantName(name).build();
        
        return userInfoExtraService.selectInviterList(request);
    }
    
    /**
     * 修改邀请人
     */
    @PostMapping(value = "/admin/merchant/userInfoExtra/modifyInviter")
    public R modifyInviter(@RequestBody @Validated(UpdateGroup.class) MerchantModifyInviterUpdateRequest merchantModifyInviterUpdateRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return userInfoExtraService.modifyInviter(merchantModifyInviterUpdateRequest, user.getUid());
    }
    
}
