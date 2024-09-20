package com.xiliulou.electricity.service.token;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.dto.WXMinProAuth2SessionResult;
import com.xiliulou.electricity.dto.WXMinProPhoneResultDTO;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.security.authentication.thirdauth.wxpro.ThirdWxProAuthenticationToken;
import com.xiliulou.security.bean.SecurityUser;
import com.xiliulou.security.constant.TokenConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author: eclair
 * @Date: 2020/12/2 15:29
 * @Description:
 */
@Service
@Slf4j
public class WxProThirdAuthenticationServiceImpl extends AbstractThirdAuthenticationService {
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Qualifier("restTemplateServiceImpl")
    @Autowired
    RestTemplateService restTemplateService;
    
    @Autowired
    RedisService redisService;
    
    
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
        if (Objects.isNull(electricityPayParams) || StrUtil.isEmpty(electricityPayParams.getMerchantMinProAppId()) || StrUtil
                .isEmpty(electricityPayParams.getMerchantMinProAppSecert())) {
            log.warn("TOKEN ERROR! not found appId,appSecret! authMap={}, params={}, tenantId={}", authMap, electricityPayParams, tenantId);
            throw new BizException("100002", "网络不佳，请重试");
        }
        
        try {
            
            String codeUrl = String
                    .format(CacheConstant.WX_MIN_PRO_AUTHORIZATION_CODE_URL, electricityPayParams.getMerchantMinProAppId(), electricityPayParams.getMerchantMinProAppSecert(),
                            code);
            
            String bodyStr = restTemplateService.getForString(codeUrl, null);
            log.info("TOKEN INFO! call wxpro get openId message={}", bodyStr);
            
            WXMinProAuth2SessionResult result = JsonUtil.fromJson(bodyStr, WXMinProAuth2SessionResult.class);
            //			WXMinProAuth2SessionResult result = new WXMinProAuth2SessionResult("open2", "session1", "uni", null, null);
            if (Objects.isNull(result) || StrUtil.isEmpty(result.getOpenid()) || StrUtil.isEmpty(result.getSession_key())) {
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
            if (Objects.isNull(wxMinProPhoneResultDTO) || StrUtil.isEmpty(wxMinProPhoneResultDTO.getPurePhoneNumber())) {
                log.error("TOKEN ERROR! 反序列化微信的手机号数据失败！s={},authmap={}", s, authMap);
                throw new AuthenticationServiceException("微信解密失败！");
            }
            
            String purePhoneNumber = wxMinProPhoneResultDTO.getPurePhoneNumber();
            log.info("TOKEN INFO! 解析微信手机号:{}", purePhoneNumber);
            
            return this.login(new LoginModel(purePhoneNumber, result.getOpenid()));
            
        } finally {
            redisService.delete(CacheConstant.CAHCE_THIRD_OAHTH_KEY + code);
        }
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
    
    @Override
    protected Integer getSource() {
        return UserOauthBind.SOURCE_WX_PRO;
    }
}
