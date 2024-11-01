/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/9/20
 */

package com.xiliulou.electricity.service.token;


import com.google.common.collect.Lists;
import com.xiliulou.core.i18n.MessageUtils;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.UserInfoExtraConstant;
import com.xiliulou.electricity.entity.NewUserActivity;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.exception.UserLoginException;
import com.xiliulou.electricity.service.NewUserActivityService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import com.xiliulou.security.authentication.thirdauth.ThirdAuthenticationService;
import com.xiliulou.security.bean.SecurityUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/9/20 09:58
 */
@Slf4j
public abstract class AbstractThirdAuthenticationService implements ThirdAuthenticationService {
    
    
    @Resource
    private UserOauthBindService userOauthBindService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private CustomPasswordEncoder customPasswordEncoder;
    
    @Resource
    private UserInfoExtraService userInfoExtraService;
    
    @Resource
    private NewUserActivityService newUserActivityService;
    
    
    @Resource
    private UserCouponService userCouponService;
    
    public SecurityUser login(LoginModel loginModel) {
        
        TtlTraceIdSupport.set();
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        try {
            String loginPhone = loginModel.getPhone();
            
            String loginOpenId = loginModel.getThirdOpenId();
            
            log.info("AbstractThirdAuthenticationService.login tenantId:{} loginPhone:{},loginOpenId:{}", tenantId, loginPhone, loginOpenId);
            
            //根据手机号查询
            User existUserByPhone = userService.queryByUserPhoneFromDB(loginPhone, User.TYPE_USER_NORMAL_WX_PRO, tenantId);
            
            if (Objects.isNull(existUserByPhone)) {
                // 用户不存在 走注册
                return register(loginPhone, loginOpenId, tenantId);
            }
            
            // 手机号已绑定的小程序
            UserOauthBind oauthBindByPhone = this.getOneUserOauthBindByPhone(loginPhone, tenantId, existUserByPhone);
            
            if (Objects.isNull(oauthBindByPhone)) {
                // 手机号未绑定，则绑定
                return this.bind(existUserByPhone, loginOpenId);
            }
            
            // 手机号已绑定，但是已经解绑
            if (this.isUnBind(oauthBindByPhone)) {
                // 解绑了 重新绑定
                return this.rebind(existUserByPhone, oauthBindByPhone, loginOpenId, tenantId);
            }
            
            // 手机号已绑定，未解绑,判定openid是否一致
            if (!Objects.equals(oauthBindByPhone.getThirdId(), loginOpenId)) {
                //手机号已绑定
                this.throwPhoneBindException();
            }
            
            //手机号已绑定，未解绑 ，openid也一致
            return this.createSecurityUser(existUserByPhone, oauthBindByPhone);
            
        } catch (UserLoginException e) {
            log.warn("AbstractThirdAuthenticationService.login UserLoginException:", e);
            throw new AuthenticationServiceException(e.getErrMsg());
        } catch (Exception e) {
            log.warn("AbstractThirdAuthenticationService.login Exception:", e);
            throw new AuthenticationServiceException("系统异常！");
        } finally {
            TtlTraceIdSupport.clear();
        }
    }
    
    protected abstract void throwPhoneBindException();
    
    
    /**
     * 生成登陆信息
     *
     * @param user
     * @param oauthBind
     * @author caobotao.cbt
     * @date 2024/9/20 11:39
     */
    private SecurityUser createSecurityUser(User user, UserOauthBind oauthBind) {
        ArrayList<String> dbAuthsSet = Lists.newArrayList();
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(dbAuthsSet.toArray(new String[0]));
        
        return new SecurityUser(oauthBind.getThirdId(), user.getPhone(), user.getUid(), user.getUserType(), user.getLoginPwd(), user.isLock(), authorities, user.getTenantId());
    }
    
    /**
     * 是否已解绑
     *
     * @param oauthBindByPhone
     * @author caobotao.cbt
     * @date 2024/9/20 11:38
     */
    private boolean isUnBind(UserOauthBind oauthBindByPhone) {
        return UserOauthBind.STATUS_UN_BIND.equals(oauthBindByPhone.getStatus()) && StringUtils.isBlank(oauthBindByPhone.getThirdId());
    }
    
    /**
     * 获取当前用户绑定的小程序信息
     *
     * @param loginPhone
     * @param tenantId
     * @param existUserByPhone
     * @author caobotao.cbt
     * @date 2024/9/20 11:36
     */
    protected UserOauthBind getOneUserOauthBindByPhone(String loginPhone, Integer tenantId, User existUserByPhone) {
        //根据登陆手机号查询 是否绑定小程序
        List<UserOauthBind> userOauthBindsByPhone = userOauthBindService.listUserByPhone(loginPhone, this.getSource(), tenantId);
        
        // 过滤出当前user对应的 UserOauthBind
        return Optional.ofNullable(userOauthBindsByPhone).orElse(Collections.emptyList()).stream().filter(u -> Objects.equals(existUserByPhone.getUid(), u.getUid())).findFirst()
                .orElse(null);
    }
    
    /**
     * 重新绑定
     *
     * @param existUserByPhone
     * @param oauthBindByPhone
     * @param loginOpenId
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/9/20 11:41
     */
    private SecurityUser rebind(User existUserByPhone, UserOauthBind oauthBindByPhone, String loginOpenId, Integer tenantId) {
        oauthBindByPhone.setThirdId(loginOpenId);
        oauthBindByPhone.setUpdateTime(System.currentTimeMillis());
        oauthBindByPhone.setStatus(UserOauthBind.STATUS_BIND);
        userOauthBindService.update(oauthBindByPhone);
        
        this.insertUserInfo(existUserByPhone);
        
        return createSecurityUser(existUserByPhone, oauthBindByPhone);
    }
    
    
    /**
     * 新增userinfo
     *
     * @param existUserByPhone
     * @author caobotao.cbt
     * @date 2024/9/20 11:50
     */
    private void insertUserInfo(User existUserByPhone) {
        //添加到user_info表中
        Long uid = existUserByPhone.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            UserInfo insertUserInfo = UserInfo.builder().uid(uid).updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(existUserByPhone.getPhone())
                    .name(existUserByPhone.getName()).delFlag(User.DEL_NORMAL).usableStatus(UserInfo.USER_USABLE_STATUS).tenantId(existUserByPhone.getTenantId()).build();
            userInfoService.insert(insertUserInfo);
            
            userInfoExtraService.insert(buildUserInfoExtra(uid, existUserByPhone.getTenantId()));
        }
    }
    
    /**
     * 绑定
     *
     * @param existUserByPhone
     * @param loginOpenId
     * @author caobotao.cbt
     * @date 2024/9/20 11:48
     */
    private SecurityUser bind(User existUserByPhone, String loginOpenId) {
        UserOauthBind build = UserOauthBind.builder().createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).phone(existUserByPhone.getPhone())
                .uid(existUserByPhone.getUid()).thirdId(loginOpenId).source(this.getSource()).status(UserOauthBind.STATUS_BIND).tenantId(existUserByPhone.getTenantId())
                .accessToken("").refreshToken("").thirdNick("").build();
        userOauthBindService.insert(build);
        this.insertUserInfo(existUserByPhone);
        
        return createSecurityUser(existUserByPhone, build);
    }
    
    /**
     * 注册用户
     *
     * @param loginPhone
     * @param loginOpenId
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/9/20 13:34
     */
    private SecurityUser register(String loginPhone, String loginOpenId, Integer tenantId) {
        User insertUser = User.builder().updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(loginPhone).lockFlag(User.USER_UN_LOCK)
                .gender(User.GENDER_MALE).lang(MessageUtils.LOCALE_ZH_CN).userType(User.TYPE_USER_NORMAL_WX_PRO).name("").salt("").avatar("").tenantId(tenantId)
                .loginPwd(customPasswordEncoder.encode("1234#56!^1mjh")).delFlag(User.DEL_NORMAL).build();
        User insert = userService.insert(insertUser);
        
        UserOauthBind oauthBind = UserOauthBind.builder().createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).phone(loginPhone).uid(insert.getUid())
                .accessToken("").refreshToken("").thirdNick("").tenantId(tenantId).thirdId(loginOpenId).source(this.getSource()).status(UserOauthBind.STATUS_BIND).build();
        userOauthBindService.insert(oauthBind);
        
        //添加到user_info表中
        UserInfo insertUserInfo = UserInfo.builder().uid(insert.getUid()).updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(loginPhone).name("")
                .tenantId(tenantId).delFlag(User.DEL_NORMAL).usableStatus(UserInfo.USER_USABLE_STATUS).build();
        userInfoService.insert(insertUserInfo);
        
        userInfoExtraService.insert(buildUserInfoExtra(insert.getUid(), tenantId));
        
        //参加新用户活动
        NewUserActivity newUserActivity = newUserActivityService.queryActivity();
        if (Objects.nonNull(newUserActivity)) {
            log.info("send the coupon to new user after logon, activity info = {}, user info = {}", newUserActivity.getId(), insert.getUid());
            //优惠券
            if (Objects.equals(newUserActivity.getDiscountType(), NewUserActivity.TYPE_COUPON) && Objects.nonNull(newUserActivity.getCouponId())) {
                //发放优惠券
                Long[] uids = new Long[1];
                uids[0] = insert.getUid();
                log.info("uids is -->{}", uids[0]);
                userCouponService.batchRelease(newUserActivity.getCouponId(), uids, newUserActivity.getId().longValue());
            }
        }
        
        return createSecurityUser(insertUser, oauthBind);
    }
    
    
    /**
     * 构建 UserInfoExtra
     *
     * @param uid
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/9/20 11:44
     */
    private UserInfoExtra buildUserInfoExtra(Long uid, Integer tenantId) {
        UserInfoExtra userInfoExtra = new UserInfoExtra();
        userInfoExtra.setUid(uid);
        userInfoExtra.setDelFlag(User.DEL_NORMAL);
        userInfoExtra.setTenantId(tenantId);
        userInfoExtra.setCreateTime(System.currentTimeMillis());
        userInfoExtra.setUpdateTime(System.currentTimeMillis());
        userInfoExtra.setActivitySource(NumberConstant.ZERO);
        userInfoExtra.setInviterUid(NumberConstant.ZERO_L);
        userInfoExtra.setLatestActivitySource(NumberConstant.ZERO);
        userInfoExtra.setEleLimit(UserInfoExtraConstant.ELE_LIMIT_YES);
        return userInfoExtra;
    }
    
    /**
     * 获取来源
     *
     * @author caobotao.cbt
     * @date 2024/9/20 10:52
     */
    protected abstract Integer getSource();
    
    
    @Data
    @AllArgsConstructor
    public static class LoginModel {
        
        private String phone;
        
        private String thirdOpenId;
        
        
    }
    
}
