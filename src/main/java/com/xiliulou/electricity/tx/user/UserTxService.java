/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/9/19
 */

package com.xiliulou.electricity.tx.user;

import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/9/19 14:14
 */
@Service
public class UserTxService {
    
    @Resource
    private UserOauthBindService userOauthBindService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private UserInfoService userInfoService;
    
    
    @Resource
    private UserInfoExtraService userInfoExtraService;
    
    
    @Transactional(rollbackFor = Exception.class)
    public void register(User user, UserInfo userInfo, UserOauthBind oauthBind, UserInfoExtra userInfoExtra) {
        userService.insert(user);
        userInfo.setUid(user.getUid());
        userInfoService.insert(userInfo);
        oauthBind.setUid(user.getUid());
        userOauthBindService.insert(oauthBind);
        userInfoExtra.setUid(user.getUid());
        userInfoExtraService.insert(userInfoExtra);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void registerUserOauthBind(UserOauthBind oauthBind, UserInfo userInfo, UserInfoExtra userInfoExtra) {
        userOauthBindService.insert(oauthBind);
        if (Objects.nonNull(userInfo)) {
            userInfoService.insert(userInfo);
        }
        if (Objects.nonNull(userInfoExtra)) {
            userInfoExtraService.insert(userInfoExtra);
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void createUserInfo(UserInfo insertUserInfo, UserInfoExtra userInfoExtra) {
        userInfoService.insert(insertUserInfo);
        userInfoExtraService.insert(userInfoExtra);
    }
}
