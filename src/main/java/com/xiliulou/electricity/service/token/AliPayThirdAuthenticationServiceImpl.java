/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/9/19
 */

package com.xiliulou.electricity.service.token;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipayEncrypt;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.constant.AliPayConstant;
import com.xiliulou.core.i18n.MessageUtils;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.pay.AlipayAppConfigBizDetails;
import com.xiliulou.electricity.config.AliPayConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.AlipayUserPhoneDTO;
import com.xiliulou.electricity.entity.NewUserActivity;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.exception.UserLoginException;
import com.xiliulou.electricity.service.AlipayAppConfigService;
import com.xiliulou.electricity.service.NewUserActivityService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.tx.user.UserTxService;
import com.xiliulou.pay.alipay.exception.AliPayException;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import com.xiliulou.security.authentication.thirdauth.ThirdAuthenticationService;
import com.xiliulou.security.authentication.thirdauth.wxpro.ThirdWxProAuthenticationToken;
import com.xiliulou.security.bean.SecurityUser;
import com.xiliulou.security.constant.TokenConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;


/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/9/19 11:14
 */
@Slf4j
@Service
public class AliPayThirdAuthenticationServiceImpl implements ThirdAuthenticationService {
    
    /**
     * 加签算法。小程序默认为 RSA2。
     */
    private static final String SIGN_TYPE = "RSA2";
    
    /**
     * 加密算法。默认为 AES
     */
    private static final String ENCRYPT_TYPE = "AES";
    
    private static final String DECRYPT_ALIPAY_FLAG = "Success";
    
    /**
     * 授权类型
     */
    private static final String GRANT_TYPE = "authorization_code";
    
    
    @Resource
    private AlipayAppConfigService alipayAppConfigService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private AliPayConfig aliPayConfig;
    
    @Resource
    private UserOauthBindService userOauthBindService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private UserTxService userTxService;
    
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
    
    @Override
    public AbstractAuthenticationToken generateThirdAuthenticationToken(HttpServletRequest request) {
        String code = obtainCode(request);
        String data = obtainData(request);
        String iv = obtainIv(request);
        String appType = obtainAppType(request);
        return new ThirdWxProAuthenticationToken(code, iv, TokenConstant.THIRD_AUTH_ALI_PAY, data, appType);
    }
    
    @Override
    public SecurityUser registerUserAndLoadUser(HashMap<String, Object> hashMap) {
        
        String code = get(hashMap, "code");
        String iv = get(hashMap, "iv");
        String data = get(hashMap, "data");
        
        if (!redisService.setNx(CacheConstant.CAHCE_THIRD_OAHTH_KEY + code, "1", 5000L, false)) {
            throw new AuthenticationServiceException("操作频繁！请稍后再试！");
        }
        
        try {
            Integer tenantId = TenantContextHolder.getTenantId();
            
            AlipayAppConfigBizDetails alipayAppConfig = this.acquireAlipayAppConfig(tenantId);
            
            //解析手机号
            String loginPhone = decryptAliPayResponseData(data, iv, alipayAppConfig);
            //解析openId
            String loginOpenId = decryptAliPayAuthCodeData(code, alipayAppConfig.getAppId(), alipayAppConfig);
            
            log.info("ALIPAY LOGIN INFO!user login info,loginPhone={},loginOpenId={}", loginPhone, loginOpenId);
            
            //根据手机号查询
            User existUserByPhone = userService.queryByUserPhoneFromDB(loginPhone, User.TYPE_USER_NORMAL_WX_PRO, tenantId);
            
            if (Objects.isNull(existUserByPhone)) {
                // 用户不存在 走注册
                return register(loginPhone, loginOpenId, tenantId);
            }
            
            //查询当前手机号绑定的openid
            UserOauthBind userOauthBindByPhone = userOauthBindService.listUserByPhone(loginPhone, UserOauthBind.SOURCE_ALI_PAY, tenantId).stream().findFirst().get();
            
            if (Objects.nonNull(userOauthBindByPhone) && !Objects.equals(existUserByPhone.getUid(), userOauthBindByPhone.getUid())) {
                // uid 不匹配
                log.error("t_user_oauth_bind data mismatch. uid is {}", existUserByPhone.getUid());
                throw new UserLoginException("100567", "该账户已绑定其他微信，请联系客服处理");
            }
            
            //根据openId 查询
            UserOauthBind userOauthBindByOpenId = userOauthBindService.queryOneByOpenIdAndSource(loginOpenId, UserOauthBind.SOURCE_ALI_PAY, tenantId);
            
            if (Objects.isNull(userOauthBindByOpenId)) {
                // 登陆 openId 不存在
                return registerUserOauthBind(existUserByPhone, userOauthBindByPhone, loginOpenId);
            }
            
            // exist 和 userOauthBind 都存在
            return this.login(existUserByPhone, userOauthBindByPhone, userOauthBindByOpenId);
            
        } finally {
            redisService.delete(CacheConstant.CAHCE_THIRD_OAHTH_KEY + code);
        }
        
    }
    
    private SecurityUser login(User exist, UserOauthBind userOauthBindByPhone, UserOauthBind userOauthBindByOpenId) {
        
        if (Objects.isNull(userOauthBindByPhone)) {
            // 通过手机号没查询到绑定记录，根据openid查到了，说明openid绑定的手机号不一致
            throw new UserLoginException("100567", "该账户已绑定其他微信，请联系客服处理");
        }
        
        if (!Objects.equals(userOauthBindByPhone.getUid(), userOauthBindByOpenId.getUid())) {
            // 通过手机号查询到的与openid查询到的不是一条
            log.error("ALIPAY LOGIN ERROR! two exists! third account uid not equals user account uid! phoneUid={},openUserId={}", userOauthBindByPhone.getUid(),
                    userOauthBindByOpenId.getUid());
            throw new UserLoginException("100567", "该账户已绑定其他微信，请联系客服处理");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(exist.getUid());
        if (Objects.isNull(userInfo)) {
            UserInfo insertUserInfo = UserInfo.builder().uid(exist.getUid()).updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(exist.getPhone())
                    .name(exist.getName()).authStatus(UserInfo.AUTH_STATUS_STATUS_INIT).delFlag(User.DEL_NORMAL).usableStatus(UserInfo.USER_USABLE_STATUS)
                    .tenantId(exist.getTenantId()).build();
            UserInfoExtra userInfoExtra = buildUserInfoExtra(exist.getUid(), exist.getTenantId());
            userTxService.createUserInfo(insertUserInfo, userInfoExtra);
        }
        
        return createSecurityUser(exist, userOauthBindByOpenId);
    }
    
    private SecurityUser registerUserOauthBind(User existUserByPhone, UserOauthBind userOauthBindByPhone, String loginOpenId) {
        
        if (Objects.isNull(userOauthBindByPhone)) {
            // 创建用户绑定关系
            return createUserOauthBind(existUserByPhone, loginOpenId);
        }
        
        // 当前绑定的openid不为空
        if (StringUtils.isNotBlank(userOauthBindByPhone.getThirdId())) {
            log.warn("TOKEN WARN! openId not exists,phone exists and phone third id exist! userId={},existOpendId", existUserByPhone.getUid(), userOauthBindByPhone.getThirdId());
            throw new UserLoginException("100567", "该账户已绑定其他微信，请联系客服处理");
        }
        
        //这里更改openId
        userOauthBindByPhone.setThirdId(loginOpenId);
        userOauthBindByPhone.setUpdateTime(System.currentTimeMillis());
        userOauthBindByPhone.setStatus(UserOauthBind.STATUS_BIND);
        userOauthBindService.update(userOauthBindByPhone);
        
        return createSecurityUser(existUserByPhone, userOauthBindByPhone);
    }
    
    private SecurityUser createUserOauthBind(User existUserByPhone, String loginOpenId) {
        
        // 手机号未找到对应的 绑定记录,生成绑定记录
        UserOauthBind oauthBind = UserOauthBind.builder().createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).phone(existUserByPhone.getPhone())
                .uid(existUserByPhone.getUid()).thirdId(loginOpenId).source(UserOauthBind.SOURCE_ALI_PAY).status(UserOauthBind.STATUS_BIND).tenantId(existUserByPhone.getTenantId())
                .accessToken("").refreshToken("").thirdNick("").build();
        
        UserInfo userInfo = this.checkBuildInsertUserInfo(existUserByPhone);
        if (Objects.nonNull(userInfo)) {
            UserInfoExtra userInfoExtra = buildUserInfoExtra(existUserByPhone.getUid(), existUserByPhone.getTenantId());
            userTxService.registerUserOauthBind(oauthBind, userInfo, userInfoExtra);
        } else {
            userTxService.registerUserOauthBind(oauthBind, null, null);
        }
        
        return createSecurityUser(existUserByPhone, oauthBind);
        
    }
    
    private UserInfo checkBuildInsertUserInfo(User exist) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(exist.getUid());
        if (Objects.nonNull(userInfo)) {
            return null;
        }
        
        return UserInfo.builder().uid(exist.getUid()).updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(exist.getPhone()).name(exist.getName())
                .delFlag(User.DEL_NORMAL).usableStatus(UserInfo.USER_USABLE_STATUS).tenantId(exist.getTenantId()).build();
    }
    
    
    private AlipayAppConfigBizDetails acquireAlipayAppConfig(Integer tenantId) {
        try {
            AlipayAppConfigBizDetails bizDetails = alipayAppConfigService.queryPreciseByTenantIdAndFranchiseeId(tenantId, MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
            
            if (Objects.isNull(bizDetails) || StringUtils.isBlank(bizDetails.getAppId()) || StringUtils.isBlank(bizDetails.getPublicKey()) || StringUtils
                    .isBlank(bizDetails.getAppPrivateKey())) {
                log.warn("ALIPAY LOGIN ERROR! not found appId,publicKey tenantId={}", tenantId);
                throw new BizException("100002", "网络不佳，请重试");
            }
            return bizDetails;
        } catch (AliPayException e) {
            log.error("ALIPAY LOGIN ERROR!AlipayAppConfig is null,tenantId={}", tenantId);
            throw new AuthenticationServiceException("系统异常,请稍后重试！");
        }
    }
    
    
    /**
     * 解密支付宝小程序获取手机号返回的加密数据
     *
     * @param content
     * @param sign
     * @return
     */
    private String decryptAliPayResponseData(String content, String sign, AlipayAppConfigBizDetails alipayAppConfig) {
        String phone = "";
        //1.判断是否为加密内容
        boolean isDataEncrypted = !content.startsWith("{");
        //2. 验签
        String signContent = content;
        //支付宝公钥
        String signVeriKey = alipayAppConfig.getPublicKey();
        //支付宝小程序对应的加解密密钥
        String decryptKey = alipayAppConfig.getLoginDecryptionKey();
        if (isDataEncrypted) {
            signContent = "\"" + signContent + "\"";
        }
        
        try {
            if (!AlipaySignature.rsaCheck(signContent, sign, signVeriKey, CharEncoding.UTF_8, SIGN_TYPE)) {
                //验签不通过（异常或者报文被篡改），终止流程（不需要做解密）
                log.error("ALIPAY TOKEN ERROR!signature verification failed");
                throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
            }
            
            //3. 解密
            String plainData = "";
            if (isDataEncrypted) {
                plainData = AlipayEncrypt.decryptContent(content, ENCRYPT_TYPE, decryptKey, CharEncoding.UTF_8);
            }
            
            //4.获取手机号
            AlipayUserPhoneDTO alipayUserPhoneDTO = JsonUtil.fromJson(plainData, AlipayUserPhoneDTO.class);
            if (!DECRYPT_ALIPAY_FLAG.equals(alipayUserPhoneDTO.getMsg())) {
                log.error("ALIPAY TOKEN ERROR!convert user phone failed,msg={}", plainData);
            }
            
            phone = alipayUserPhoneDTO.getMobile();
        } catch (AlipayApiException e) {
            log.error("ALIPAY TOKEN ERROR!acquire user phone failed", e);
            throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
        }
        
        return phone;
    }
    
    
    private String decryptAliPayAuthCodeData(String code, String appId, AlipayAppConfigBizDetails alipayAppConfig) {
        String openId = null;
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(buildAlipayConfig(appId, alipayAppConfig));
            // 构造请求参数以调用接口
            AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
            
            // 设置刷新令牌
            //request.setRefreshToken("201208134b203fe6c11548bcabd8da5bb087a83b");
            // 设置授权码
            request.setCode(code);
            // 设置授权方式
            request.setGrantType(GRANT_TYPE);
            
            AlipaySystemOauthTokenResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                log.error("ALIPAY TOKEN ERROR!acquire openId failed,msg={}", response);
                throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
            }
            
            openId = response.getOpenId();
        } catch (AlipayApiException e) {
            log.error("ALIPAY TOKEN ERROR!acquire openId failed", e);
            throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
        }
        
        return openId;
    }
    
    
    private AlipayConfig buildAlipayConfig(String appId, AlipayAppConfigBizDetails alipayAppConfig) {
        String privateKey = alipayAppConfig.getAppPrivateKey();
        String alipayPublicKey = alipayAppConfig.getPublicKey();
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(aliPayConfig.getServerUrl());
        alipayConfig.setAppId(appId);
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setFormat(AliPayConstant.CONFIG_FORMAT);
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setCharset(CharEncoding.UTF_8);
        alipayConfig.setSignType(SIGN_TYPE);
        return alipayConfig;
    }
    
    
    private SecurityUser register(String phone, String openId, Integer tenantId) {
        User insertUser = User.builder().updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(phone).lockFlag(User.USER_UN_LOCK)
                .gender(User.GENDER_MALE).lang(MessageUtils.LOCALE_ZH_CN).userType(User.TYPE_USER_NORMAL_WX_PRO).name("").salt("").avatar("").tenantId(tenantId)
                .loginPwd(customPasswordEncoder.encode("1234#56!^1mjh")).delFlag(User.DEL_NORMAL).build();
        
        UserOauthBind oauthBind = UserOauthBind.builder().createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).phone(phone).accessToken("")
                .refreshToken("").thirdNick("").tenantId(tenantId).thirdId(openId).source(UserOauthBind.SOURCE_ALI_PAY).status(UserOauthBind.STATUS_BIND).build();
        
        UserInfo insertUserInfo = UserInfo.builder().updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(phone).name("").tenantId(tenantId)
                .delFlag(User.DEL_NORMAL).usableStatus(UserInfo.USER_USABLE_STATUS).build();
        
        UserInfoExtra userInfoExtra = UserInfoExtra.builder().delFlag(User.DEL_NORMAL).tenantId(tenantId).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).activitySource(NumberConstant.ZERO).inviterUid(NumberConstant.ZERO_L).latestActivitySource(NumberConstant.ZERO).build();
        
        userTxService.register(insertUser, insertUserInfo, oauthBind, userInfoExtra);
        
        //参加新用户活动
        NewUserActivity newUserActivity = newUserActivityService.queryActivity();
        if (Objects.nonNull(newUserActivity) && Objects.equals(newUserActivity.getDiscountType(), NewUserActivity.TYPE_COUPON) && Objects.nonNull(newUserActivity.getCouponId())) {
            userCouponService.batchRelease(newUserActivity.getCouponId(), new Long[] {insertUser.getUid()});
        }
        
        return createSecurityUser(insertUser, oauthBind);
    }
    
    private SecurityUser createSecurityUser(User user, UserOauthBind oauthBind) {
        ArrayList<String> dbAuthsSet = Lists.newArrayList();
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(dbAuthsSet.toArray(new String[0]));
        
        return new SecurityUser(oauthBind.getThirdId(), user.getPhone(), user.getUid(), user.getUserType(), user.getLoginPwd(), user.isLock(), authorities, user.getTenantId());
    }
    
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
        return userInfoExtra;
    }
    
    
    private String get(HashMap<String, Object> authMap, String key) {
        return (String) authMap.get(key);
    }
}
