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
import com.xiliulou.electricity.entity.AlipayAppConfig;
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
import com.xiliulou.pay.alipay.exception.AliPayException;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import com.xiliulou.security.authentication.thirdauth.ThirdAuthenticationService;
import com.xiliulou.security.authentication.thirdauth.wxpro.ThirdWxProAuthenticationToken;
import com.xiliulou.security.bean.SecurityUser;
import com.xiliulou.security.constant.TokenConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-07-02-15:33
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
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    UserOauthBindService userOauthBindService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    CustomPasswordEncoder customPasswordEncoder;
    
    @Autowired
    UserInfoExtraService userInfoExtraService;
    
    @Autowired
    UserCouponService userCouponService;
    
    @Autowired
    NewUserActivityService newUserActivityService;
    
    @Autowired
    AlipayAppConfigService alipayAppConfigService;
    
    @Autowired
    AliPayConfig aliPayConfig;
    
    @Override
    public AbstractAuthenticationToken generateThirdAuthenticationToken(HttpServletRequest request) {
        String code = obtainCode(request);
        String data = obtainData(request);
        String iv = obtainIv(request);
        String appType = obtainAppType(request);
        return new ThirdWxProAuthenticationToken(code, iv, TokenConstant.THIRD_AUTH_ALI_PAY, data, appType);
    }
    
    @Override
    public SecurityUser registerUserAndLoadUser(HashMap<String, Object> authMap) {
        String code = (String) authMap.get("code");
        String iv = (String) authMap.get("iv");
        String data = (String) authMap.get("data");
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        if (!redisService.setNx(CacheConstant.CAHCE_THIRD_OAHTH_KEY + code, "1", 5000L, false)) {
            throw new AuthenticationServiceException("操作频繁！请稍后再试！");
        }
    
        AlipayAppConfigBizDetails alipayAppConfig = this.acquireAlipayAppConfig(tenantId);
        
        //获取支付宝小程序appId
        String appId = alipayAppConfig.getAppId();
        
        try {
            //解析手机号
            String phone = decryptAliPayResponseData(data, iv, alipayAppConfig);
            
            //解析openId
            String openId = decryptAliPayAuthCodeData(code, appId, alipayAppConfig);
            log.info("ALIPAY LOGIN INFO!user login info,phone={},openId={}", phone, openId);
            
            //检查openId是否存在
            Pair<Boolean, UserOauthBind> existsOpenId = checkAliPayOpenIdExists(openId, UserOauthBind.SOURCE_ALI_PAY, tenantId);
            
            //检查手机号是否存在
            Pair<Boolean, User> existPhone = checkPhoneExists(phone, User.TYPE_USER_NORMAL_WX_PRO, tenantId);
            
            //1.两个都不存在，创建用户
            if (Boolean.TRUE.equals(!existPhone.getLeft()) && Boolean.TRUE.equals(!existsOpenId.getLeft())) {
                return createUserAndOauthBind(phone, openId, tenantId);
            }
            
            //2.两个都存在
            if (existPhone.getLeft() && existsOpenId.getLeft()) {
                //uid不同，异常处理
                if (!existPhone.getRight().getUid().equals(existsOpenId.getRight().getUid())) {
                    log.error("ALIPAY LOGIN ERROR! two exists! third account uid not equals user account uid! thirdUid={},userId={}", existsOpenId.getRight().getUid(),
                            existPhone.getRight().getUid());
                    throw new AuthenticationServiceException("用户信息异常，请联系客户处理!");
                }
                
                Long uid = existPhone.getRight().getUid();
                Pair<Boolean, UserInfo> existUserInfo = checkUserInfoExists(uid);
                //添加到user_info表中
                if (Boolean.FALSE.equals(existUserInfo.getLeft())) {
                    UserInfo insertUserInfo = UserInfo.builder().uid(uid).updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis())
                            .phone(existPhone.getRight().getPhone()).name(existPhone.getRight().getName()).authStatus(UserInfo.AUTH_STATUS_STATUS_INIT).delFlag(User.DEL_NORMAL)
                            .usableStatus(UserInfo.USER_USABLE_STATUS).tenantId(tenantId).build();
                    userInfoService.insert(insertUserInfo);
                    
                    UserInfoExtra userInfoExtra = UserInfoExtra.builder().uid(uid).delFlag(User.DEL_NORMAL).tenantId(tenantId).createTime(System.currentTimeMillis())
                            .updateTime(System.currentTimeMillis()).activitySource(NumberConstant.ZERO).inviterUid(NumberConstant.ZERO_L).latestActivitySource(NumberConstant.ZERO)
                            .build();
                    userInfoExtraService.insert(userInfoExtra);
                }
                
                //相同登录
                return createSecurityUser(existPhone.getRight(), existsOpenId.getRight());
            }
            
            //3.如果openId存在手机号不存在，则新增账号
            if (existsOpenId.getLeft() && !existPhone.getLeft()) {
                return createUserAndOauthBind(phone, openId, tenantId);
                
                //                User user = userService.queryByUidFromCache(existsOpenId.getRight().getUid());
                //                if (Objects.isNull(user)) {
                //                    log.error("ALIPAY LOGIN ERROR! can't found user!uid={}", existsOpenId.getRight().getUid());
                //                    throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                //                }
                //
                //                //这里的uid必须相同
                //                if (!Objects.equals(user.getUid(), existsOpenId.getRight().getUid())) {
                //                    log.error("ALIPAY LOGIN ERROR! openId exists,phone not exists! third account uid not equals user account uid! thirdUid={},userId={}",
                //                            existsOpenId.getRight().getUid(), user.getUid());
                //                    throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                //                }
                //
                //                User updateUser = User.builder().uid(user.getUid()).phone(phone).updateTime(System.currentTimeMillis()).build();
                //                userService.updateUser(updateUser, user);
                //                //                user.setPhone(phone);
                //                //如果第三方账号的手机号和现在的手机号不同，就让他同步
                //                if (!existsOpenId.getRight().getPhone().equals(phone)) {
                //                    UserOauthBind existOpenUser = existsOpenId.getRight();
                //                    existOpenUser.setPhone(phone);
                //                    userOauthBindService.update(existOpenUser);
                //                }
                //
                //                Long uid = existsOpenId.getRight().getUid();
                //                Pair<Boolean, UserInfo> existUserInfo = checkUserInfoExists(uid);
                //                if (!existUserInfo.getLeft()) {
                //                    UserInfo insertUserInfo = UserInfo.builder().uid(uid).updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(phone)
                //                            .name(user.getName()).delFlag(User.DEL_NORMAL).usableStatus(UserInfo.USER_USABLE_STATUS).tenantId(tenantId).build();
                //                    userInfoService.insert(insertUserInfo);
                //
                //                } else {
                //                    UserInfo updateUserInfo = existUserInfo.getRight();
                //                    if (!Objects.equals(phone, updateUserInfo.getPhone())) {
                //                        updateUserInfo.setPhone(phone);
                //                        updateUserInfo.setUid(uid);
                //                        updateUserInfo.setTenantId(tenantId);
                //                        updateUserInfo.setUpdateTime(System.currentTimeMillis());
                //                        userInfoService.update(updateUserInfo);
                //                    }
                //                }
                //
                //                return createSecurityUser(user, existsOpenId.getRight());
            }
            
            //4.如果openId不存在手机号存在，则
            if (!existsOpenId.getLeft() && existPhone.getLeft()) {
                List<UserOauthBind> userOauthBinds = userOauthBindService.listUserByPhone(existPhone.getRight().getPhone(), UserOauthBind.SOURCE_ALI_PAY, tenantId);
                UserOauthBind userOauthBind = null;
                if (!CollectionUtils.isEmpty(userOauthBinds)) {
                    // 支付宝用户只会存在一条记录
                    userOauthBind = userOauthBinds.get(0);
                    if (!Objects.equals(userOauthBind.getUid(), existPhone.getRight().getUid())) {
                        log.error("ALIPAY LOGIN ERROR! openId not exists,phone exists! third account uid not equals user account uid! thirdUid={},userId={}",
                                userOauthBind.getUid(), existPhone.getRight().getUid());
                        throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                    }
                    //----
                    // 如果openid不存在,手机号存在,并且传入手机号的openid已经绑定过,则直接拦截
                    List<UserOauthBind> emptyUserList = userOauthBinds.stream().filter(userOauthBindTemp -> StringUtils.isBlank(userOauthBindTemp.getThirdId())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(emptyUserList)) {
                        log.warn("TOKEN WARN! openId not exists,phone exists and phone third id exist! userId={}", existPhone.getRight().getUid());
                        //  throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                        throw new UserLoginException("100567", "该账户已绑定其他支付宝，请联系客服处理");
                    }
    
                    //这里uid必须相同
                    userOauthBind = emptyUserList.stream().filter(emptyUser -> !emptyUser.getUid().equals(existPhone.getRight().getUid())).findAny().orElse(null);
                    if (ObjectUtils.isNotEmpty(userOauthBind)) {
                        log.error(
                                "TOKEN ERROR! openId not exists,phone exists! third account uid not equals user account uid! thirdUid={},userId={}",
                                userOauthBind.getUid(), existPhone.getRight().getUid());
                        throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                    }
    
                    userOauthBind = emptyUserList.stream().filter(emptyUser -> emptyUser.getUid().equals(existPhone.getRight().getUid())).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(userOauthBind)) {
                        log.error("t_user_oauth_bind data mismatch. uid is {}",existPhone.getRight().getUid());
                        throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                    }
    
    
    
                    //这里更改openId
                    userOauthBind.setThirdId(openId);
                    userOauthBind.setUpdateTime(System.currentTimeMillis());
                    userOauthBind.setStatus(UserOauthBind.STATUS_BIND);
                    userOauthBindService.update(userOauthBind);
                } else {
                    userOauthBind = UserOauthBind.builder().createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).phone(existPhone.getRight().getPhone())
                            .uid(existPhone.getRight().getUid()).thirdId(openId).source(UserOauthBind.SOURCE_ALI_PAY).status(UserOauthBind.STATUS_BIND).tenantId(tenantId)
                            .accessToken("").refreshToken("").thirdNick("").build();
                    userOauthBindService.insert(userOauthBind);
                }
                
                Long uid = existPhone.getRight().getUid();
                Pair<Boolean, UserInfo> existUserInfo = checkUserInfoExists(uid);
                if (!existUserInfo.getLeft()) {
                    UserInfo insertUserInfo = UserInfo.builder().uid(uid).updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis())
                            .phone(existPhone.getRight().getPhone()).name(existPhone.getRight().getName()).delFlag(User.DEL_NORMAL).usableStatus(UserInfo.USER_USABLE_STATUS)
                            .tenantId(tenantId).build();
                    userInfoService.insert(insertUserInfo);
                }
                
                return createSecurityUser(existPhone.getRight(), userOauthBind);
            }
        } finally {
            redisService.delete(CacheConstant.CAHCE_THIRD_OAHTH_KEY + code);
        }
        
        log.error("ALIPAY LOGIN ERROR!alipay login fail,params={}", authMap);
        throw new AuthenticationServiceException("系统异常,请稍后重试！");
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
    
    private Pair<Boolean, UserInfo> checkUserInfoExists(Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        return Objects.nonNull(userInfo) ? Pair.of(true, userInfo) : Pair.of(false, null);
    }
    
    public Pair<Boolean, UserOauthBind> checkAliPayOpenIdExists(String openid, Integer source, Integer tenantId) {
        UserOauthBind userOauthBind = userOauthBindService.queryOneByOpenIdAndSource(openid, source, tenantId);
        return Objects.nonNull(userOauthBind) ? Pair.of(true, userOauthBind) : Pair.of(false, null);
    }
    
    private Pair<Boolean, User> checkPhoneExists(String purePhoneNumber, Integer source, Integer tenantId) {
        User user = userService.queryByUserPhoneFromDB(purePhoneNumber, source, tenantId);
        return Objects.nonNull(user) ? Pair.of(true, user) : Pair.of(false, null);
    }
    
    private SecurityUser createUserAndOauthBind(String phone, String openId, Integer tenantId) {
        User insertUser = User.builder().updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(phone).lockFlag(User.USER_UN_LOCK)
                .gender(User.GENDER_MALE).lang(MessageUtils.LOCALE_ZH_CN).userType(User.TYPE_USER_NORMAL_WX_PRO).name("").salt("").avatar("").tenantId(tenantId)
                .loginPwd(customPasswordEncoder.encode("1234#56!^1mjh")).delFlag(User.DEL_NORMAL).build();
        User insert = userService.insert(insertUser);
        
        UserOauthBind oauthBind = UserOauthBind.builder().createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).phone(phone).uid(insert.getUid())
                .accessToken("").refreshToken("").thirdNick("").tenantId(tenantId).thirdId(openId).source(UserOauthBind.SOURCE_ALI_PAY).status(UserOauthBind.STATUS_BIND).build();
        userOauthBindService.insert(oauthBind);
        
        UserInfo insertUserInfo = UserInfo.builder().uid(insert.getUid()).updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(phone).name("")
                .tenantId(tenantId).delFlag(User.DEL_NORMAL).usableStatus(UserInfo.USER_USABLE_STATUS).build();
        userInfoService.insert(insertUserInfo);
        
        UserInfoExtra userInfoExtra = UserInfoExtra.builder().uid(insert.getUid()).delFlag(User.DEL_NORMAL).tenantId(tenantId).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).activitySource(NumberConstant.ZERO).inviterUid(NumberConstant.ZERO_L).latestActivitySource(NumberConstant.ZERO).build();
        userInfoExtraService.insert(userInfoExtra);
        
        //参加新用户活动
        NewUserActivity newUserActivity = newUserActivityService.queryActivity();
        if (Objects.nonNull(newUserActivity) && Objects.equals(newUserActivity.getDiscountType(), NewUserActivity.TYPE_COUPON) && Objects.nonNull(newUserActivity.getCouponId())) {
            userCouponService.batchRelease(newUserActivity.getCouponId(), new Long[] {insert.getUid()});
        }
        
        return createSecurityUser(insertUser, oauthBind);
    }
    
    private SecurityUser createSecurityUser(User user, UserOauthBind oauthBind) {
        ArrayList<String> dbAuthsSet = Lists.newArrayList();
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(dbAuthsSet.toArray(new String[0]));
        
        return new SecurityUser(oauthBind.getThirdId(), user.getPhone(), user.getUid(), user.getUserType(), user.getLoginPwd(), user.isLock(), authorities, user.getTenantId());
    }
    
    private String decryptAliPayAuthCodeData(String code, String appId, AlipayAppConfigBizDetails alipayAppConfig) {
        String openId = null;
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig(appId, alipayAppConfig));
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
    
    private AlipayConfig getAlipayConfig(String appId, AlipayAppConfigBizDetails alipayAppConfig) {
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
}
