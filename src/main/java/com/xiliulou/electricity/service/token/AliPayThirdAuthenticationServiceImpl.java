/**
 * Create date: 2024/9/19
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
import com.xiliulou.electricity.dto.AlipayUserPhoneDTO;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.exception.UserLoginException;
import com.xiliulou.electricity.service.AlipayAppConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.pay.alipay.exception.AliPayException;
import com.xiliulou.security.authentication.thirdauth.wxpro.ThirdWxProAuthenticationToken;
import com.xiliulou.security.bean.SecurityUser;
import com.xiliulou.security.constant.TokenConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Objects;


/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/9/19 11:14
 */
@Slf4j
@Service
public class AliPayThirdAuthenticationServiceImpl extends AbstractThirdAuthenticationService {
    
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
        String code = null;
        try {
            TtlTraceIdSupport.set();
            
            code = get(hashMap, "code");
            String iv = get(hashMap, "iv");
            String data = get(hashMap, "data");
            String appid = get(hashMap, "appid");
            
            if (!redisService.setNx(CacheConstant.CAHCE_THIRD_OAHTH_KEY + code, "1", 5000L, false)) {
                throw new AuthenticationServiceException("操作频繁！请稍后再试！");
            }
            
            Integer tenantId = TenantContextHolder.getTenantId();
            
            AlipayAppConfigBizDetails alipayAppConfig = this.acquireAlipayAppConfig(tenantId);
            
            //解析手机号
            String loginPhone = decryptAliPayResponseData(data, iv, alipayAppConfig);
    
            String loginOpenId = null;
    
            //解析openId
            if (ObjectUtils.isNotEmpty(appid)) {
                // 第三方应用登录
                loginOpenId = decryptAliPayAuthCodeDataForThirdParty(code, appid, alipayAppConfig);
            } else {
                loginOpenId = decryptAliPayAuthCodeData(code, alipayAppConfig.getAppId(), alipayAppConfig);
            }
            
            log.info("ALIPAY LOGIN INFO!user login info,loginPhone={},loginOpenId={}", loginPhone, loginOpenId);
            
            return this.login(new LoginModel(loginPhone, loginOpenId));
            
        } catch (Exception e) {
            log.warn("AliPayThirdAuthenticationServiceImpl.registerUserAndLoadUser WARN! Exception:", e);
            throw e;
        } finally {
            redisService.delete(CacheConstant.CAHCE_THIRD_OAHTH_KEY + code);
            TtlTraceIdSupport.clear();
        }
        
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
                throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
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
    
    /**
     * 第三方应用登录解析
     * @param code
     * @param appId
     * @param alipayAppConfig
     * @return
     */
    private String decryptAliPayAuthCodeDataForThirdParty(String code, String appId, AlipayAppConfigBizDetails alipayAppConfig) {
        // 第三方登录解析
        if (ObjectUtils.isEmpty(alipayAppConfig.getAppAuthToken())) {
            throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
        }
        
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
    
            AlipaySystemOauthTokenResponse response = alipayClient.execute(request, null, alipayAppConfig.getAppAuthToken());
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
    
    
    @Override
    protected void throwPhoneBindException() {
        throw new UserLoginException("100567", "该账户已绑定其他支付宝，请联系客服处理");
    }
    
    @Override
    protected Integer getSource() {
        return UserOauthBind.SOURCE_ALI_PAY;
    }
    
    
    private String get(HashMap<String, Object> authMap, String key) {
        return (String) authMap.get(key);
    }
}
