package com.xiliulou.electricity.service.token;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.i18n.MessageUtils;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.WXMinProAuth2SessionResult;
import com.xiliulou.electricity.dto.WXMinProPhoneResultDTO;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.NewUserActivity;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.exception.UserLoginException;
import com.xiliulou.electricity.service.EleUserAuthOldService;
import com.xiliulou.electricity.service.EleUserAuthService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.NewUserActivityService;
import com.xiliulou.electricity.service.OldCardService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoOldService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import com.xiliulou.security.authentication.thirdauth.ThirdAuthenticationService;
import com.xiliulou.security.authentication.thirdauth.wxpro.ThirdWxProAuthenticationToken;
import com.xiliulou.security.bean.SecurityUser;
import com.xiliulou.security.constant.TokenConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: eclair
 * @Date: 2020/12/2 15:29
 * @Description:
 */
@Service
@Slf4j
public class WxProThirdAuthenticationServiceImpl implements ThirdAuthenticationService {
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Qualifier("restTemplateServiceImpl")
    @Autowired
    RestTemplateService restTemplateService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    UserOauthBindService userOauthBindService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    CustomPasswordEncoder customPasswordEncoder;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    UserInfoOldService userInfoOldService;
    
    @Autowired
    UserInfoExtraService userInfoExtraService;
    
    @Autowired
    EleUserAuthOldService eleUserAuthOldService;
    
    @Autowired
    EleUserAuthService eleUserAuthService;
    
    @Autowired
    OldCardService oldCardService;
    
    @Autowired
    NewUserActivityService newUserActivityService;
    
    @Autowired
    UserCouponService userCouponService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Override
    public AbstractAuthenticationToken generateThirdAuthenticationToken(HttpServletRequest request) {
        String code = obtainCode(request);
        String data = obtainData(request);
        String iv = obtainIv(request);
        String appType = obtainAppType(request);
        return new ThirdWxProAuthenticationToken(code, iv, TokenConstant.THIRD_AUTH_WX_PRO, data, appType);
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
        
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryPreciseCacheByTenantIdAndFranchiseeId(tenantId, MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
        if (Objects.isNull(electricityPayParams) || StrUtil.isEmpty(electricityPayParams.getMerchantMinProAppId())
                || StrUtil.isEmpty(electricityPayParams.getMerchantMinProAppSecert())) {
            log.warn("TOKEN ERROR! not found appId,appSecret! authMap={}, params={}, tenantId={}", authMap, electricityPayParams, tenantId);
            throw new BizException("100002","网络不佳，请重试");
        }
        
        try {
            
            String codeUrl = String.format(CacheConstant.WX_MIN_PRO_AUTHORIZATION_CODE_URL,
                    electricityPayParams.getMerchantMinProAppId(), electricityPayParams.getMerchantMinProAppSecert(),
                    code);
            
            String bodyStr = restTemplateService.getForString(codeUrl, null);
            log.info("TOKEN INFO! call wxpro get openId message={}", bodyStr);
            
            WXMinProAuth2SessionResult result = JsonUtil.fromJson(bodyStr, WXMinProAuth2SessionResult.class);
            //			WXMinProAuth2SessionResult result = new WXMinProAuth2SessionResult("open2", "session1", "uni", null, null);
            if (Objects.isNull(result) || StrUtil.isEmpty(result.getOpenid()) || StrUtil.isEmpty(
                    result.getSession_key())) {
                log.error("TOKEN ERROR! wxResult has error! bodyStr={},authMap={}", bodyStr, authMap);
                throw new AuthenticationServiceException("微信返回异常！");
            }
            
            //解析手机号
            String s = decryptWxData(data, iv, result.getSession_key());
            if (StrUtil.isEmpty(s)) {
                throw new AuthenticationServiceException("WX0001");
            }
            
            WXMinProPhoneResultDTO wxMinProPhoneResultDTO = JsonUtil.fromJson(s, WXMinProPhoneResultDTO.class);
            //			WXMinProPhoneResultDTO wxMinProPhoneResultDTO = new WXMinProPhoneResultDTO("18664317712", "18664317712", "zh");
            if (Objects.isNull(wxMinProPhoneResultDTO) || StrUtil.isEmpty(
                    wxMinProPhoneResultDTO.getPurePhoneNumber())) {
                log.error("TOKEN ERROR! 反序列化微信的手机号数据失败！s={},authmap={}", s, authMap);
                throw new AuthenticationServiceException("微信解密失败！");
            }
            
            String purePhoneNumber = wxMinProPhoneResultDTO.getPurePhoneNumber();
            log.info("TOKEN INFO! 解析微信手机号:{}", purePhoneNumber);
            
            //先检查openId存在吗
            Pair<Boolean, List<UserOauthBind>> existsOpenId = checkOpenIdExists(result.getOpenid(), tenantId);
            //检查手机号是否存在
            Pair<Boolean, User> existPhone = checkPhoneExists(purePhoneNumber, tenantId);
            log.info("new user logon, existsOpenId = {}, existPhone = {}", JsonUtil.toJson(existsOpenId), JsonUtil.toJson(existPhone));
            //如果两个都不存在，创建用户
            if (!existPhone.getLeft() && !existsOpenId.getLeft()) {
                return createUserAndOauthBind(result, wxMinProPhoneResultDTO);
            }
            
            //两个都存在，
            if (existPhone.getLeft() && existsOpenId.getLeft()) {
                Long uidExist = existPhone.getRight().getUid();
                // 通过手机号会查询出来多个（骑手端、商户端）
                List<UserOauthBind> userOauthBinds = userOauthBindService.listUserByPhone(existPhone.getRight().getPhone(), UserOauthBind.SOURCE_WX_PRO, tenantId);
                log.info("userOauthBinds is {}", JsonUtil.toJson(userOauthBinds));
                if (CollectionUtils.isEmpty(userOauthBinds)) {
                    log.warn("TOKEN WARN! not find user auth bind info! openId={},userId={}", result.getOpenid(), uidExist);
                    throw new UserLoginException("100567", "该账户尚未绑定");
                }
                
                UserOauthBind userOauthBindLogin = userOauthBinds.stream().filter(userOauthBindTemp -> result.getOpenid().equals(userOauthBindTemp.getThirdId())).findFirst()
                        .orElse(null);
                log.info("userOauthBindLogin is {}", JsonUtil.toJson(userOauthBindLogin));
                if (ObjectUtils.isEmpty(userOauthBindLogin)) {
                    // 匹配不到，通过UID匹配，查询是否解绑
                    UserOauthBind userOauthUnBind = userOauthBinds.stream()
                            .filter(userOauthBindTemp -> uidExist.equals(userOauthBindTemp.getUid()) && UserOauthBind.STATUS_UN_BIND.equals(userOauthBindTemp.getStatus())).findFirst()
                            .orElse(null);
                    if (ObjectUtils.isEmpty(userOauthUnBind)) {
                        log.warn("TOKEN WARN! find user auth bind many ! openId is {}, userId is {}", result.getOpenid(), uidExist);
                        throw new UserLoginException("ELECTRICITY.0001", "用户登录异常");
                    }
                    userOauthBindLogin = userOauthUnBind;
                }
                
                List<UserOauthBind> oauthBindList = existsOpenId.getRight();
                List<Long> uidList = oauthBindList.stream().map(UserOauthBind::getUid).collect(Collectors.toList());
                
                //uid不同，异常处理
                if (StringUtils.isNotBlank(userOauthBindLogin.getThirdId()) && !uidList.contains(uidExist)) {
                    log.error("TOKEN ERROR! two exists! third account uid not equals user account uid! uidList={},userId={}", JsonUtil.toJson(uidList), uidExist);
                    throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                }
                
                //添加到user_info表中
                Long uid = uidExist;
                Pair<Boolean, UserInfo> existUserInfo = checkUserInfoExists(uid);
                if (!existUserInfo.getLeft()) {
                    UserInfo insertUserInfo = UserInfo.builder().uid(uid).updateTime(System.currentTimeMillis())
                            .createTime(System.currentTimeMillis()).phone(existPhone.getRight().getPhone())
                            .name(existPhone.getRight().getName())
                            .authStatus(UserInfo.AUTH_STATUS_STATUS_INIT).delFlag(User.DEL_NORMAL)
                            .usableStatus(UserInfo.USER_USABLE_STATUS).tenantId(tenantId).build();
                    UserInfo userInfo = userInfoService.insert(insertUserInfo);
                    
                    userInfoExtraService.insert(buildUserInfoExtra(uid, tenantId));
                }
                
                if(StringUtils.isBlank(userOauthBindLogin.getThirdId())){
                    //这里更改openId
                    userOauthBindLogin.setThirdId(result.getOpenid());
                    userOauthBindLogin.setUpdateTime(System.currentTimeMillis());
                    userOauthBindLogin.setStatus(UserOauthBind.STATUS_BIND);
                    userOauthBindService.update(userOauthBindLogin);
                }
                
                //相同登录
                return createSecurityUser(existPhone.getRight(), oauthBindList.get(0));
            }
            
            //如果openId存在.手机号不存在,则新增账号
            if (existsOpenId.getLeft() && !existPhone.getLeft()) {
                return createUserAndOauthBind(result, wxMinProPhoneResultDTO);
            }
            
            //openid不存在的时候,手机号存在
            if (!existsOpenId.getLeft() && existPhone.getLeft()) {
                UserOauthBind userOauthBind = null;
                List<UserOauthBind> userOauthBinds = userOauthBindService.listUserByPhone(existPhone.getRight().getPhone(),
                        UserOauthBind.SOURCE_WX_PRO, tenantId);
                if (CollectionUtils.isNotEmpty(userOauthBinds)) {
                    // 如果openid不存在,手机号存在,并且传入手机号的openid已经绑定过,则直接拦截
                    List<UserOauthBind> emptyUserList = userOauthBinds.stream().filter(userOauthBindTemp -> StringUtils.isBlank(userOauthBindTemp.getThirdId())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(emptyUserList)) {
                        log.warn("TOKEN WARN! openId not exists,phone exists and phone third id exist! userId={}", existPhone.getRight().getUid());
                        //  throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                        throw new UserLoginException("100567", "该账户已绑定其他微信，请联系客服处理");
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
                    userOauthBind.setThirdId(result.getOpenid());
                    userOauthBind.setUpdateTime(System.currentTimeMillis());
                    userOauthBind.setStatus(UserOauthBind.STATUS_BIND);
                    userOauthBindService.update(userOauthBind);
                } else {
                    userOauthBind = UserOauthBind.builder().createTime(System.currentTimeMillis())
                            .updateTime(System.currentTimeMillis()).phone(existPhone.getRight().getPhone())
                            .uid(existPhone.getRight().getUid()).thirdId(result.getOpenid())
                            .source(UserOauthBind.SOURCE_WX_PRO).status(UserOauthBind.STATUS_BIND).tenantId(tenantId)
                            .accessToken("").refreshToken("").thirdNick("").build();
                    userOauthBindService.insert(userOauthBind);
                }
                
                //添加到user_info表中
                Long uid = existPhone.getRight().getUid();
                Pair<Boolean, UserInfo> existUserInfo = checkUserInfoExists(uid);
                if (!existUserInfo.getLeft()) {
                    UserInfo insertUserInfo = UserInfo.builder().uid(uid).updateTime(System.currentTimeMillis())
                            .createTime(System.currentTimeMillis()).phone(existPhone.getRight().getPhone())
                            .name(existPhone.getRight().getName())
                            .delFlag(User.DEL_NORMAL).usableStatus(UserInfo.USER_USABLE_STATUS).tenantId(tenantId)
                            .build();
                    userInfoService.insert(insertUserInfo);
                    
                    userInfoExtraService.insert(buildUserInfoExtra(uid, tenantId));
                }
                return createSecurityUser(existPhone.getRight(), userOauthBind);
                
            }
        } catch (Exception e) {
            if (e instanceof UserLoginException) {
                log.warn("该账户已绑定其他微信，请联系客服处理", e);
                throw new UserLoginException("100567", "该账户已绑定其他微信，请联系客服处理");
            }
            
            log.error("ELE AUTH ERROR: ", e);
        } finally {
            redisService.delete(CacheConstant.CAHCE_THIRD_OAHTH_KEY + code);
        }
        log.error("TOKEN ERROR! SYSTEM ERROR! params={}", authMap);
        throw new AuthenticationServiceException("系统异常！");
    }
    
    private Pair<Boolean, UserInfo> checkUserInfoExists(Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        return Objects.nonNull(userInfo) ? Pair.of(true, userInfo) : Pair.of(false, null);
    }
    
    private SecurityUser createSecurityUser(User user, UserOauthBind oauthBind) {
        ArrayList<String> dbAuthsSet = Lists.newArrayList();
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(
                dbAuthsSet.toArray(new String[0]));
        
        return new SecurityUser(oauthBind.getThirdId(), user.getPhone(), user.getUid(), user.getUserType(),
                user.getLoginPwd(), user.isLock(), authorities, user.getTenantId());
    }
    
    private SecurityUser createUserAndOauthBind(WXMinProAuth2SessionResult result,
            WXMinProPhoneResultDTO wxMinProPhoneResultDTO) {
        Integer tenantId = TenantContextHolder.getTenantId();
        //不存在，创建新用户
        User insertUser = User.builder().updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis())
                .phone(wxMinProPhoneResultDTO.getPurePhoneNumber()).lockFlag(User.USER_UN_LOCK).gender(User.GENDER_MALE)
                .lang(MessageUtils.LOCALE_ZH_CN).userType(User.TYPE_USER_NORMAL_WX_PRO).name("").salt("").avatar("")
                .tenantId(tenantId).loginPwd(customPasswordEncoder.encode("1234#56!^1mjh")).delFlag(User.DEL_NORMAL)
                .build();
        User insert = userService.insert(insertUser);
        
        UserOauthBind oauthBind = UserOauthBind.builder().createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).phone(wxMinProPhoneResultDTO.getPurePhoneNumber())
                .uid(insert.getUid()).accessToken("").refreshToken("").thirdNick("").tenantId(tenantId)
                .thirdId(result.getOpenid()).source(UserOauthBind.SOURCE_WX_PRO).status(UserOauthBind.STATUS_BIND)
                .build();
        userOauthBindService.insert(oauthBind);
        
        //添加到user_info表中
        UserInfo insertUserInfo = UserInfo.builder().uid(insert.getUid()).updateTime(System.currentTimeMillis())
                .createTime(System.currentTimeMillis()).phone(wxMinProPhoneResultDTO.getPurePhoneNumber()).name("")
                .tenantId(tenantId).delFlag(User.DEL_NORMAL)
                .usableStatus(UserInfo.USER_USABLE_STATUS).build();
        UserInfo userInfo = userInfoService.insert(insertUserInfo);
        
        userInfoExtraService.insert(buildUserInfoExtra(insert.getUid(), tenantId));
        
        //参加新用户活动
        NewUserActivity newUserActivity = newUserActivityService.queryActivity();
        if (Objects.nonNull(newUserActivity)) {
            log.info("send the coupon to new user after logon, activity info = {}, user info = {}", newUserActivity.getId(), insert.getUid());
            //优惠券
            if (Objects.equals(newUserActivity.getDiscountType(), NewUserActivity.TYPE_COUPON) && Objects.nonNull(
                    newUserActivity.getCouponId())) {
                //发放优惠券
                Long[] uids = new Long[1];
                uids[0] = insert.getUid();
                log.info("uids is -->{}", uids[0]);
                userCouponService.batchRelease(newUserActivity.getCouponId(), uids);
            }
        }
        
        return createSecurityUser(insertUser, oauthBind);
    }
    
    
    private Pair<Boolean, User> checkPhoneExists(String purePhoneNumber, Integer tenantId) {
        User user = userService.queryByUserPhoneFromDB(purePhoneNumber, User.TYPE_USER_NORMAL_WX_PRO, tenantId);
        return Objects.nonNull(user) ? Pair.of(true, user) : Pair.of(false, null);
    }
    
    public Pair<Boolean, List<UserOauthBind>> checkOpenIdExists(String openid, Integer tenantId) {
        List<UserOauthBind> userOauthBindList = userOauthBindService.selectListOauthByOpenIdAndSource(openid,
                UserOauthBind.SOURCE_WX_PRO, tenantId);
        return CollectionUtils.isNotEmpty(userOauthBindList) ? Pair.of(true, userOauthBindList) : Pair.of(false, null);
    }
    
    public String decryptWxData(String encrydata, String iv, Object key) {
        byte[] encrypData = Base64.decode(encrydata);
        byte[] ivData = Base64.decode(iv);
        byte[] sessionKey = Base64.decode(key.toString());
        String decryptData = null;
        try {
            decryptData = decrypt(sessionKey, ivData, encrypData);
        } catch (Exception e) {
            log.error("解密微信信息失败！", e);
        }
        return decryptData;
    }
    
    public static String decrypt(byte[] key, byte[] iv, byte[] encData) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            params.init(new IvParameterSpec(iv));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, params);
            return new String(cipher.doFinal(encData), "UTF-8");
        } catch (Exception e) {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, params);
            return new String(cipher.doFinal(encData), "UTF-8");
        }
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
}
