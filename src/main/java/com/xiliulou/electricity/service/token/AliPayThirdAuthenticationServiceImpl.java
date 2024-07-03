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
import com.xiliulou.core.i18n.MessageUtils;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.NewUserActivity;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.service.NewUserActivityService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import com.xiliulou.security.authentication.thirdauth.ThirdAuthenticationService;
import com.xiliulou.security.bean.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
    
    
    @Override
    public SecurityUser registerUserAndLoadUser(HashMap<String, Object> authMap) {
        String code = (String) authMap.get("code");
        String iv = (String) authMap.get("iv");
        String data = (String) authMap.get("data");
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        if (!redisService.setNx(CacheConstant.CAHCE_THIRD_OAHTH_KEY + code, "1", 5000L, false)) {
            throw new AuthenticationServiceException("操作频繁！请稍后再试！");
        }
        
        //获取支付宝小程序appId TODO
        String appId = "";
        
        //解析手机号 TODO
        String phone = decryptAliPayResponseData(data, iv);
        
        //解析openId
        String openId = decryptAliPayAuthCodeData(code, appId);
        log.info("ALIPAY LOGIN INFO!user login info,phone={},openId={}", phone, openId);
        
        try {
            //检查openId是否存在
            Pair<Boolean, List<UserOauthBind>> existsOpenId = checkOpenIdExists(openId, UserOauthBind.SOURCE_ALI_PAY, tenantId);
            
            //检查手机号是否存在
            Pair<Boolean, User> existPhone = checkPhoneExists(phone, User.TYPE_USER_NORMAL_ALI_PAY, tenantId);
            
            //1.两个都不存在，创建用户
            if (!existPhone.getLeft() && !existsOpenId.getLeft()) {
                return createUserAndOauthBind(phone, openId, tenantId);
            }
            
            //2.两个都存在
            if (existPhone.getLeft() && existsOpenId.getLeft()) {
                List<Long> uidList = existsOpenId.getRight().stream().map(UserOauthBind::getUid).collect(Collectors.toList());
                //uid不同，异常处理
                if (CollectionUtils.isNotEmpty(uidList) && uidList.contains(existPhone.getRight().getUid())) {
                    log.error("ALIPAY LOGIN ERROR! two exists! third account uid not equals user account uid,phone={},openId={}", phone, openId);
                    throw new AuthenticationServiceException("用户信息异常，请联系客户处理!");
                }
                
                //添加到user_info表中
                Long uid = existPhone.getRight().getUid();
                Pair<Boolean, UserInfo> existUserInfo = checkUserInfoExists(uid);
                if (!existUserInfo.getLeft()) {
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
                //TODO　openId
                return createSecurityUser(existPhone.getRight(), existsOpenId.getRight().get(0));
            }
            
            //3.如果openId存在手机号不存在，则替换以前的手机号
            if (existsOpenId.getLeft() && !existPhone.getLeft()) {
                //这里不能为空
                //                User user = userService.queryByUidFromCache(existsOpenId.getRight().getUid());
                //                if (Objects.isNull(user)) {
                //                    log.error("TOKEN ERROR! can't found user!uid={}", existsOpenId.getRight().getUid());
                //                    throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                //                }
                //
                //                //这里的uid必须相同
                //                if (!Objects.equals(user.getUid(), existsOpenId.getRight().getUid())) {
                //                    log.error(
                //                            "TOKEN ERROR! openId exists,phone not exists! third account uid not equals user account uid! thirdUid={},userId={}",
                //                            existsOpenId.getRight().getUid(), user.getUid());
                //                    throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                //                }
                //
                //                User updateUser = User.builder().uid(user.getUid()).phone(purePhoneNumber)
                //                        .updateTime(System.currentTimeMillis()).build();
                //                userService.updateUser(updateUser, user);
                //                user.setPhone(purePhoneNumber);
                //                //如果第三方账号的手机号和现在的手机号不同，就让他同步
                //                if (!existsOpenId.getRight().getPhone().equals(purePhoneNumber)) {
                //                    UserOauthBind existOpenUser = existsOpenId.getRight();
                //                    existOpenUser.setPhone(purePhoneNumber);
                //                    userOauthBindService.update(existOpenUser);
                //                }
                //                //添加到user_info表中
                //                Long uid = existsOpenId.getRight().getUid();
                //                Pair<Boolean, UserInfo> existUserInfo = checkUserInfoExists(uid);
                //                if (!existUserInfo.getLeft()) {
                //                    UserInfo insertUserInfo = UserInfo.builder().uid(uid).updateTime(System.currentTimeMillis())
                //                            .createTime(System.currentTimeMillis()).phone(purePhoneNumber).name(user.getName())
                //                            .delFlag(User.DEL_NORMAL)
                //                            .usableStatus(UserInfo.USER_USABLE_STATUS).tenantId(tenantId).build();
                //                    UserInfo userInfo = userInfoService.insert(insertUserInfo);
                //
                //                } else {
                //                    UserInfo updateUserInfo = existUserInfo.getRight();
                //                    if (!Objects.equals(purePhoneNumber, updateUserInfo.getPhone())) {
                //                        updateUserInfo.setPhone(purePhoneNumber);
                //                        updateUserInfo.setUid(uid);
                //                        updateUserInfo.setTenantId(tenantId);
                //                        updateUserInfo.setUpdateTime(System.currentTimeMillis());
                //                        userInfoService.update(updateUserInfo);
                //                    }
                //                }
                //                return createSecurityUser(user, existsOpenId.getRight());
                
            }
            
            //4.如果openId不存在手机号存在，则
            if (!existsOpenId.getLeft() && existPhone.getLeft()) {
                
                //                UserOauthBind userOauthBind = userOauthBindService.queryByUserPhone(existPhone.getRight().getPhone(),
                //                        UserOauthBind.SOURCE_WX_PRO, tenantId);
                //                if (Objects.nonNull(userOauthBind)) {
                //                    //这里uid必须相同
                //                    if (!Objects.equals(userOauthBind.getUid(), existPhone.getRight().getUid())) {
                //                        log.error(
                //                                "TOKEN ERROR! openId not exists,phone exists! third account uid not equals user account uid! thirdUid={},userId={}",
                //                                userOauthBind.getUid(), existPhone.getRight().getUid());
                //                        throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                //                    }
                //
                //                    //这里更改openId
                //                    userOauthBind.setThirdId(result.getOpenid());
                //                    userOauthBind.setUpdateTime(System.currentTimeMillis());
                //                    userOauthBindService.update(userOauthBind);
                //                } else {
                //                    userOauthBind = UserOauthBind.builder().createTime(System.currentTimeMillis())
                //                            .updateTime(System.currentTimeMillis()).phone(existPhone.getRight().getPhone())
                //                            .uid(existPhone.getRight().getUid()).thirdId(result.getOpenid())
                //                            .source(UserOauthBind.SOURCE_WX_PRO).status(UserOauthBind.STATUS_BIND).tenantId(tenantId)
                //                            .accessToken("").refreshToken("").thirdNick("").build();
                //                    userOauthBindService.insert(userOauthBind);
                //                }
                //                //添加到user_info表中
                //                Long uid = existPhone.getRight().getUid();
                //                Pair<Boolean, UserInfo> existUserInfo = checkUserInfoExists(uid);
                //                if (!existUserInfo.getLeft()) {
                //                    UserInfo insertUserInfo = UserInfo.builder().uid(uid).updateTime(System.currentTimeMillis())
                //                            .createTime(System.currentTimeMillis()).phone(existPhone.getRight().getPhone())
                //                            .name(existPhone.getRight().getName())
                //                            .delFlag(User.DEL_NORMAL).usableStatus(UserInfo.USER_USABLE_STATUS).tenantId(tenantId)
                //                            .build();
                //                    UserInfo userInfo = userInfoService.insert(insertUserInfo);
                //
                //                }
                //                return createSecurityUser(existPhone.getRight(), userOauthBind);
                
            }
        } finally {
            redisService.delete(CacheConstant.CAHCE_THIRD_OAHTH_KEY + code);
        }
        
        log.error("ALIPAY LOGIN ERROR!alipay login fail,params={}", authMap);
        throw new AuthenticationServiceException("系统异常,请稍后重试！");
    }
    
    private Pair<Boolean, UserInfo> checkUserInfoExists(Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        return Objects.nonNull(userInfo) ? Pair.of(true, userInfo) : Pair.of(false, null);
    }
    
    public Pair<Boolean, List<UserOauthBind>> checkOpenIdExists(String openid, Integer source, Integer tenantId) {
        List<UserOauthBind> userOauthBindList = userOauthBindService.selectListOauthByOpenIdAndSource(openid, source, tenantId);
        return CollectionUtils.isNotEmpty(userOauthBindList) ? Pair.of(true, userOauthBindList) : Pair.of(false, null);
    }
    
    private Pair<Boolean, User> checkPhoneExists(String purePhoneNumber, Integer source, Integer tenantId) {
        User user = userService.queryByUserPhoneFromDB(purePhoneNumber, source, tenantId);
        return Objects.nonNull(user) ? Pair.of(true, user) : Pair.of(false, null);
    }
    
    private SecurityUser createUserAndOauthBind(String phone, String openId, Integer tenantId) {
        User insertUser = User.builder().updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(phone).lockFlag(User.USER_UN_LOCK)
                .gender(User.GENDER_MALE).lang(MessageUtils.LOCALE_ZH_CN).userType(User.TYPE_USER_NORMAL_ALI_PAY).name("").salt("").avatar("").tenantId(tenantId)
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
    
    private String decryptAliPayAuthCodeData(String code, String appId) {
        
        // 初始化SDK TODO 优化单例
        AlipaySystemOauthTokenResponse response = null;
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig(appId));
            
            // 构造请求参数以调用接口
            AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
            
            // 设置刷新令牌
            //request.setRefreshToken("201208134b203fe6c11548bcabd8da5bb087a83b");
            // 设置授权码
            request.setCode(code);
            // 设置授权方式
            request.setGrantType("authorization_code");
            
            response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                log.error("ALIPAY TOKEN ERROR!acquire openId failed,msg={}", response.getBody());
                throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
            }
        } catch (AlipayApiException e) {
            log.error("ALIPAY TOKEN ERROR!acquire openId failed", e);
            throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
        }
        
        return response.getOpenId();
    }
    
    /**
     * 解密支付宝小程序获取手机号返回的加密数据
     *
     * @param content
     * @param sign
     * @return
     */
    private String decryptAliPayResponseData(String content, String sign) {
        //1.判断是否为加密内容
        boolean isDataEncrypted = !content.startsWith("{");
        
        //2. 验签
        String signContent = content;
        //支付宝公钥
        String signVeriKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjAvf3qdHU+2h/2RvPsONumsEfThA/x4gL65UHrDISNyOf4kWITKiH9bahLXF5Y0v/NZS9tTUND7bq7EK2Qi7GJz6thcFyVe2hUgKMbOdfnZTXMmgVR8FOIbyoclhRZd3TSJRN2DvfZ6EW0uz7dDk9QUsKEshN759czM1wlGWBugrUXY7HEF4HJoHJ/5QuRBaTN36VFiPGPzjtvpvW3Mo9632a6uj4co6jEzXg2W8RGLyiRCWRUFBs9PRJGf2BccvZjanV3AjPGybpyOxEeJVQTziqcyAYLYCYwZE4CQkYKbpksOmLOfWZkR2qawp97K6dyYX0IjV/A5uxUrRBjBJwQIDAQAB";
        //支付宝小程序对应的加解密密钥
        String decryptKey = "qnyJJ6MM7oN7dLp22MbAww==";
        if (isDataEncrypted) {
            signContent = "\"" + signContent + "\"";
        }
        
        String plainData = content;
        try {
            if (!AlipaySignature.rsaCheck(signContent, sign, signVeriKey, CharEncoding.UTF_8, SIGN_TYPE)) {
                //验签不通过（异常或者报文被篡改），终止流程（不需要做解密）
                log.error("ALIPAY TOKEN ERROR!signature verification failed");
                throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
            }
            
            //3. 解密
            if (isDataEncrypted) {
                plainData = AlipayEncrypt.decryptContent(content, ENCRYPT_TYPE, decryptKey, CharEncoding.UTF_8);
            }
        } catch (AlipayApiException e) {
            log.error("ALIPAY TOKEN ERROR!acquire user phone failed", e);
            throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
        }
        
        return plainData;
    }
    
    private static AlipayConfig getAlipayConfig(String appId) {
        //TODO 读取配置文件
        String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCrRFmF7E6HnOCiBCr084z9RFBSTXgpMKogP3weZAADEssUQH7uFBPKHez1K04UUUfnhRRqmsuz9l4D/3/jXFmdsZJROtqEHR4+uh8efEFsilKoI4jyCGfP6/gJzy7yDsS/LU6TqoODkhY5NWtmQiOyVDNgifb994YvcZq2Hs4CMJ4NxZrj89MnieUWVTwtig1vuE3wBhKX4/fTZJsX+ATWpfd7pR5evO1rjCmObZmeJ0wh/jRC0/eX7YS4ml2FPfPSRl3LQYWO+dRPgQGxRZCeXnn2EKTjtz/efKjP12miXdF7htsX2bT+J6HRlomzq2pYDLkrHJDa/xrYXawcU09VAgMBAAECggEAVBh3rN7foI9TbbqGLUj5zdKhbghEHFWc88C4fWO07c1okkUpDlYlcXVISQo+iJNwryoVYFMp+u/aMjRe45ERH/F1WxV+/qgdlcdmSF3S8izpcU4hjFa8QsgnPwnQj2LZENZ6Yt+zPAObjfPBDLElPgdCQwD7mrDT3q/1u79cgI327R53scI+fDyaTpq1/aKLGtj4k0reIYAD/FMQUaNB49z14Zwt6WaHuFSWT3D6sx3KuZVRGjUKUTSjGlS7AsU4WyHAxHZnitP48zga1pimtVhPDpZ+OzrjxHFyqNoPFLWDelmHBUewV/8ons4q32SQ5/9yULfW5CYvllWTcGpr8QKBgQDys5IvmPeS2AlLfzNnTr0UcZDyDHAxCp2mSVMZHdqcv02FAtPejEvW/hdOeqgJwDKqp9Sazqy03BNiTrVvsDxcAZNIwP1X//WFL6h+31AijfxE7hVJNHfJSUfRnmPjLT9BzDeJ9GUoTPvoaqTk4kZ+Z2QrH9NeVxTQR/vtNIjerwKBgQC0psBIw+vs0wacHEuDJrY/HX8wg88FMD6rblP6CDcGudUr76cG0WuCkH0r/3GZ486GwKVMUsmz5zuBFt/kANqOO/uy0qZYgdjHmuvHNr1qIlanJmwX7Ia36qr/x0ppu6FHzV9oUE0kaslkdMuzKCSLnQeuZ50EV9WkO0BZ9A8TOwKBgALdyA4z2kirsIBpwiuoLGd/Z9zT9Mc/ftkl6ItVZO2Q/NNjUyk/su2ZFqFgpXdoA7EsRkCFzFheeQQiNdZZ2HylsB2d2eAeL8Ig6/aDoKin0KDnxuyUaA3Chcyd+EQIlsSqKsXAUymErzzxdX0WhwqbIf24ZICqup4zG3CTvEIVAoGBALFE6G7/AqX0Ngo+ocLi2/d3RHYhAaa/vt+Odg1mvkh1Vr+0fZxtKCiJDKt+EMXIC8OjixEoNBG7mGKGRdGBHPZx2f2SQ/WaBVVpqnBkQN7DL3D6fRvE2DXlq0MvFtBGdG73EuZT1j8kItfW3ITDoYj24LC9sBCw+E4ebnlWyuw9AoGAX1vbzP2lHcHIOVombcDmiLN/uddNBS9wis5isncPZVwpF2vPGU6voHOJYrTvRwnX8YsMjfAR83FW+KmErVrpeN6/xebh1xMCxRJHQWeTzhRlxwIdrzQlSz2e1Xo9UmEyuL0yn9VvIyn1ephmhWxgIWeMHJajC3cM+PyQj5vXaYY=";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjAvf3qdHU+2h/2RvPsONumsEfThA/x4gL65UHrDISNyOf4kWITKiH9bahLXF5Y0v/NZS9tTUND7bq7EK2Qi7GJz6thcFyVe2hUgKMbOdfnZTXMmgVR8FOIbyoclhRZd3TSJRN2DvfZ6EW0uz7dDk9QUsKEshN759czM1wlGWBugrUXY7HEF4HJoHJ/5QuRBaTN36VFiPGPzjtvpvW3Mo9632a6uj4co6jEzXg2W8RGLyiRCWRUFBs9PRJGf2BccvZjanV3AjPGybpyOxEeJVQTziqcyAYLYCYwZE4CQkYKbpksOmLOfWZkR2qawp97K6dyYX0IjV/A5uxUrRBjBJwQIDAQAB";
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl("https://openapi.alipay.com/gateway.do");
        alipayConfig.setAppId(appId);
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setFormat("json");
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setCharset(CharEncoding.UTF_8);
        alipayConfig.setSignType(SIGN_TYPE);
        return alipayConfig;
    }
}
