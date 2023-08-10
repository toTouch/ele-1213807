package com.xiliulou.electricity.service.token;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.i18n.MessageUtils;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.WXMinProAuth2SessionResult;
import com.xiliulou.electricity.dto.WXMinProPhoneResultDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import com.xiliulou.security.authentication.thirdauth.ThirdAuthenticationService;
import com.xiliulou.security.bean.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

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

    @Autowired
    UserExtraService userExtraService;

    @Override
    public SecurityUser registerUserAndLoadUser(HashMap<String, Object> authMap) {
        String code = (String) authMap.get("code");
        String iv = (String) authMap.get("iv");
        String data = (String) authMap.get("data");

        Integer tenantId = TenantContextHolder.getTenantId();

        if (!redisService.setNx(CacheConstant.CAHCE_THIRD_OAHTH_KEY + code, "1", 5000L, false)) {
            throw new AuthenticationServiceException("操作频繁！请稍后再试！");
        }

        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams) || StrUtil.isEmpty(electricityPayParams.getMerchantMinProAppId())
                || StrUtil.isEmpty(electricityPayParams.getMerchantMinProAppSecert())) {
            log.warn("TOKEN ERROR! not found appId,appSecret! authMap={}", authMap);
            throw new AuthenticationServiceException("未能查找到appId和appSecret！");
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
            Pair<Boolean, UserOauthBind> existsOpenId = checkOpenIdExists(result.getOpenid(), tenantId);
            //检查手机号是否存在
            Pair<Boolean, User> existPhone = checkPhoneExists(purePhoneNumber, tenantId);

            //如果两个都不存在，创建用户
            if (!existPhone.getLeft() && !existsOpenId.getLeft()) {
                return createUserAndOauthBind(result, wxMinProPhoneResultDTO);
            }

            //两个都存在，
            if (existPhone.getLeft() && existsOpenId.getLeft()) {
                //uid不同，异常处理
                if (!existPhone.getRight().getUid().equals(existsOpenId.getRight().getUid())) {
                    log.error(
                            "TOKEN ERROR! two exists! third account uid not equals user account uid! thirdUid={},userId={}",
                            existsOpenId.getRight().getUid(), existPhone.getRight().getUid());
                    throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                }

                //添加到user_info表中
                Long uid = existPhone.getRight().getUid();
                Pair<Boolean, UserInfo> existUserInfo = checkUserInfoExists(uid);
                if (!existUserInfo.getLeft()) {
                    UserInfo insertUserInfo = UserInfo.builder().uid(uid).updateTime(System.currentTimeMillis())
                            .createTime(System.currentTimeMillis()).phone(existPhone.getRight().getPhone())
                            .name(existPhone.getRight().getName())
                            .authStatus(UserInfo.AUTH_STATUS_STATUS_INIT).delFlag(User.DEL_NORMAL)
                            .usableStatus(UserInfo.USER_USABLE_STATUS).tenantId(tenantId).build();
                    UserInfo userInfo = userInfoService.insert(insertUserInfo);

                }
                //相同登录
                return createSecurityUser(existPhone.getRight(), existsOpenId.getRight());
            }

            //如果openId存在.手机号不存在,替换掉以前的手机号
            if (existsOpenId.getLeft() && !existPhone.getLeft()) {
                //这里不能为空
                User user = userService.queryByUidFromCache(existsOpenId.getRight().getUid());
                if (Objects.isNull(user)) {
                    log.error("TOKEN ERROR! can't found user!uid={}", existsOpenId.getRight().getUid());
                    throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                }

                //这里的uid必须相同
                if (!Objects.equals(user.getUid(), existsOpenId.getRight().getUid())) {
                    log.error(
                            "TOKEN ERROR! openId exists,phone not exists! third account uid not equals user account uid! thirdUid={},userId={}",
                            existsOpenId.getRight().getUid(), user.getUid());
                    throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                }

                User updateUser = User.builder().uid(user.getUid()).phone(purePhoneNumber)
                        .updateTime(System.currentTimeMillis()).build();
                userService.updateUser(updateUser, user);
                user.setPhone(purePhoneNumber);
                //如果第三方账号的手机号和现在的手机号不同，就让他同步
                if (!existsOpenId.getRight().getPhone().equals(purePhoneNumber)) {
                    UserOauthBind existOpenUser = existsOpenId.getRight();
                    existOpenUser.setPhone(purePhoneNumber);
                    userOauthBindService.update(existOpenUser);
                }
                //添加到user_info表中
                Long uid = existsOpenId.getRight().getUid();
                Pair<Boolean, UserInfo> existUserInfo = checkUserInfoExists(uid);
                if (!existUserInfo.getLeft()) {
                    UserInfo insertUserInfo = UserInfo.builder().uid(uid).updateTime(System.currentTimeMillis())
                            .createTime(System.currentTimeMillis()).phone(purePhoneNumber).name(user.getName())
                            .delFlag(User.DEL_NORMAL)
                            .usableStatus(UserInfo.USER_USABLE_STATUS).tenantId(tenantId).build();
                    UserInfo userInfo = userInfoService.insert(insertUserInfo);

//                    Pair<Boolean, FranchiseeUserInfo> existFranchiseeUserInfo = checkFranchiseeUserInfoExists(
//                            insertUserInfo.getId());
//                    if (!existFranchiseeUserInfo.getLeft()) {
//                        FranchiseeUserInfo insertFranchiseeUserInfo = FranchiseeUserInfo.builder()
//                                .userInfoId(userInfo.getId()).updateTime(System.currentTimeMillis())
//                                .createTime(System.currentTimeMillis()).serviceStatus(FranchiseeUserInfo.STATUS_IS_INIT)
//                                .delFlag(User.DEL_NORMAL).tenantId(tenantId).build();
//                        franchiseeUserInfoService.insert(insertFranchiseeUserInfo);
//                    }

                } else {
                    UserInfo updateUserInfo = existUserInfo.getRight();
                    if (!Objects.equals(purePhoneNumber, updateUserInfo.getPhone())) {
                        updateUserInfo.setPhone(purePhoneNumber);
                        updateUserInfo.setUid(uid);
                        updateUserInfo.setTenantId(tenantId);
                        updateUserInfo.setUpdateTime(System.currentTimeMillis());
                        userInfoService.update(updateUserInfo);
                    }
                }
                return createSecurityUser(user, existsOpenId.getRight());

            }

            //openid不存在的时候,手机号存在
            if (!existsOpenId.getLeft() && existPhone.getLeft()) {

                UserOauthBind userOauthBind = userOauthBindService.queryByUserPhone(existPhone.getRight().getPhone(),
                        UserOauthBind.SOURCE_WX_PRO, tenantId);
                if (Objects.nonNull(userOauthBind)) {
                    //这里uid必须相同
                    if (!Objects.equals(userOauthBind.getUid(), existPhone.getRight().getUid())) {
                        log.error(
                                "TOKEN ERROR! openId not exists,phone exists! third account uid not equals user account uid! thirdUid={},userId={}",
                                userOauthBind.getUid(), existPhone.getRight().getUid());
                        throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
                    }

                    //这里更改openId
                    userOauthBind.setThirdId(result.getOpenid());
                    userOauthBind.setUpdateTime(System.currentTimeMillis());
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
                    UserInfo userInfo = userInfoService.insert(insertUserInfo);

//                    Pair<Boolean, FranchiseeUserInfo> existFranchiseeUserInfo = checkFranchiseeUserInfoExists(
//                            insertUserInfo.getId());
//                    if (!existFranchiseeUserInfo.getLeft()) {
//                        FranchiseeUserInfo insertFranchiseeUserInfo = FranchiseeUserInfo.builder()
//                                .userInfoId(userInfo.getId()).updateTime(System.currentTimeMillis())
//                                .createTime(System.currentTimeMillis()).serviceStatus(FranchiseeUserInfo.STATUS_IS_INIT)
//                                .delFlag(User.DEL_NORMAL).tenantId(tenantId).build();
//                        franchiseeUserInfoService.insert(insertFranchiseeUserInfo);
//                    }

                }
                return createSecurityUser(existPhone.getRight(), userOauthBind);

            }

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

        //保存用户扩展信息
        UserExtra userExtra = UserExtra.builder().uid(insert.getUid()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).tenantId(tenantId).build();
        userExtraService.insert(userExtra);

        //参加新用户活动
        NewUserActivity newUserActivity = newUserActivityService.queryActivity();
        if (Objects.nonNull(newUserActivity)) {


//            if (Objects.equals(newUserActivity.getDiscountType(), NewUserActivity.TYPE_COUNT) && Objects.nonNull(
//                    newUserActivity.getCount()) && Objects.nonNull(newUserActivity.getDays())) {
//
//                UserBatteryMemberCard userBatteryMemberCard = new UserBatteryMemberCard();
//
//                userBatteryMemberCard.setUid(userInfo.getUid());
//                userBatteryMemberCard.setCreateTime(System.currentTimeMillis());
//                userBatteryMemberCard.setUpdateTime(System.currentTimeMillis());
//                userBatteryMemberCard.setTenantId(tenantId);
//                userBatteryMemberCard.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
//                userBatteryMemberCard.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
//                userBatteryMemberCard.setMemberCardId(UserBatteryMemberCard.SEND_REMAINING_NUMBER);
//                userBatteryMemberCard.setCardPayCount(NumberConstant.ZERO);
//
//
//                userBatteryMemberCard.setRemainingNumber(newUserActivity.getCount());
//                userBatteryMemberCard.setMemberCardExpireTime(
//                        System.currentTimeMillis() + (newUserActivity.getDays() * (24 * 60 * 60 * 1000L)));
//                userBatteryMemberCardService.insertOrUpdate(userBatteryMemberCard);
//            }

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

//        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.insert(insertFranchiseeUserInfo);


        return createSecurityUser(insertUser, oauthBind);
    }


    private Pair<Boolean, User> checkPhoneExists(String purePhoneNumber, Integer tenantId) {
//        User user = userService.queryByUserPhone(purePhoneNumber, User.TYPE_USER_NORMAL_WX_PRO, tenantId);
        User user = userService.queryByUserPhoneFromDB(purePhoneNumber, User.TYPE_USER_NORMAL_WX_PRO, tenantId);
        return Objects.nonNull(user) ? Pair.of(true, user) : Pair.of(false, null);
    }

    private Pair<Boolean, UserOauthBind> checkOpenIdExists(String openid, Integer tenantId) {
        UserOauthBind userOauthBind = userOauthBindService.queryOauthByOpenIdAndSource(openid,
                UserOauthBind.SOURCE_WX_PRO, tenantId);
        return Objects.nonNull(userOauthBind) ? Pair.of(true, userOauthBind) : Pair.of(false, null);
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
}
