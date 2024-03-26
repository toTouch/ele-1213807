package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
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
public class JsonMerchantUserInfoController extends BaseController {
    
    @Resource
    private UserInfoService userInfoService;
    
    /**
     * 是否显示”修改邀请人“按钮
     */
    @GetMapping(value = "/admin/merchant/userinfo/canModifyInviter")
    public R canModifyInviter(@RequestParam("uid") Long uid) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            throw new CustomBusinessException("当前用户不存在");
        }
        
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER) && !Objects.equals(user.getType(), User.TYPE_USER_NORMAL_ADMIN)) {
            log.info("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            throw new CustomBusinessException("用户权限不足");
        }
        
        return R.ok(userInfoService.canModifyInviter(uid));
    }
    
    /**
     * 修改邀请人
     */
    @GetMapping(value = "/admin/merchant/userinfo/modifyInviter")
    public R modifyInviter() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            throw new CustomBusinessException("当前用户不存在");
        }
        
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER) && !Objects.equals(user.getType(), User.TYPE_USER_NORMAL_ADMIN)) {
            log.info("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            throw new CustomBusinessException("用户权限不足");
        }
        
        return R.ok(userInfoService.canModifyInviter(uid));
    }
    
}
