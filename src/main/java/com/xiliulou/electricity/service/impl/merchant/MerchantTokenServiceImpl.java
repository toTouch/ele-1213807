package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.dto.WXMinProAuth2SessionResult;
import com.xiliulou.electricity.dto.WXMinProPhoneResultDTO;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.merchant.ChannelEmployeeAmount;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.query.merchant.MerchantLoginRequest;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.UserChannelService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.MerchantTokenService;
import com.xiliulou.electricity.service.token.WxProThirdAuthenticationServiceImpl;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeeVO;
import com.xiliulou.electricity.vo.merchant.MerchantLoginVO;
import com.xiliulou.security.authentication.JwtTokenManager;
import com.xiliulou.security.bean.TokenUser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author : eclair
 * @date : 2024/2/18 10:19
 */
@Service
@Slf4j
public class MerchantTokenServiceImpl implements MerchantTokenService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Qualifier("restTemplateServiceImpl")
    @Autowired
    RestTemplateService restTemplateService;
    
    @Autowired
    UserOauthBindService userOauthBindService;
    
    @Autowired
    WxProThirdAuthenticationServiceImpl wxProThirdAuthenticationService;
    
    @Autowired
    MerchantService merchantService;
    
    @Autowired
    ChannelEmployeeService channelEmployeeService;
    
    @Autowired
    JwtTokenManager jwtTokenManager;
    
    @Override
    public Triple<Boolean, String, Object> login(MerchantLoginRequest merchantLoginRequest) {
        Integer tenantId = TenantContextHolder.getTenantId();
        if (!redisService.setNx(CacheConstant.CAHCE_THIRD_OAHTH_KEY + merchantLoginRequest.getCode(), "1", 5000L, false)) {
            return Triple.of(false, null, "操作频繁,请稍后再试");
        }
        
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams) || StrUtil.isEmpty(electricityPayParams.getMerchantMinProAppId()) || StrUtil.isEmpty(
                electricityPayParams.getMerchantMinProAppSecert())) {
            return Triple.of(false, "100002", "网络不佳，请重试");
        }
        
        try {
            String codeUrl = String.format(CacheConstant.WX_MIN_PRO_AUTHORIZATION_CODE_URL, electricityPayParams.getMerchantMinProAppId(),
                    electricityPayParams.getMerchantMinProAppSecert(), merchantLoginRequest.getCode());
            
            String bodyStr = restTemplateService.getForString(codeUrl, null);
            log.info("TOKEN INFO! call wxpro get openId message={}", bodyStr);
            
            WXMinProAuth2SessionResult result = JsonUtil.fromJson(bodyStr, WXMinProAuth2SessionResult.class);
            if (Objects.isNull(result) || StrUtil.isEmpty(result.getOpenid()) || StrUtil.isEmpty(result.getSession_key())) {
                log.error("TOKEN ERROR! wxResult has error! bodyStr={}", bodyStr);
                return Triple.of(false, null, "微信返回异常！");
            }
            
            // 解析手机号
            String s = wxProThirdAuthenticationService.decryptWxData(merchantLoginRequest.getData(), merchantLoginRequest.getIv(), result.getSession_key());
            if (StrUtil.isEmpty(s)) {
                return Triple.of(false, null, "解析微信数据失败");
            }
            
            WXMinProPhoneResultDTO wxMinProPhoneResultDTO = JsonUtil.fromJson(s, WXMinProPhoneResultDTO.class);
            if (Objects.isNull(wxMinProPhoneResultDTO) || StrUtil.isEmpty(wxMinProPhoneResultDTO.getPurePhoneNumber())) {
                log.error("TOKEN ERROR! 反序列化微信的手机号数据失败！s={}", s);
                return Triple.of(false, null, "解析微信数据失败");
            }
            
            String purePhoneNumber = wxMinProPhoneResultDTO.getPurePhoneNumber();
            log.info("TOKEN INFO! 解析微信手机号:{}", purePhoneNumber);
            
            User user = userService.queryByUserPhone(purePhoneNumber, merchantLoginRequest.getUserType(), tenantId);
            if (Objects.isNull(user)) {
                return Triple.of(false, null, "用户不存在");
            }
            
            if (user.isLock()) {
                return Triple.of(false, null, "当前登录账号已禁用，请联系客服处理");
            }
            
            // 用户是否绑定了业务信息
            UserBindBusinessDTO userBindBusinessDTO = checkUserBindingBusiness(user);
            if (!userBindBusinessDTO.isBinding()) {
                return Triple.of(false, null, "未找到绑定账号，请检查");
            }
            
            // 查看是否有绑定的第三方信息,如果没有绑定创建一个
            if (!wxProThirdAuthenticationService.checkOpenIdExists(result.getOpenid(), tenantId).getLeft()) {
                UserOauthBind oauthBind = UserOauthBind.builder().createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                        .phone(wxMinProPhoneResultDTO.getPurePhoneNumber()).uid(user.getUid()).accessToken("").refreshToken("").thirdNick("").tenantId(tenantId)
                        .thirdId(result.getOpenid()).source(UserOauthBind.SOURCE_WX_PRO).status(UserOauthBind.STATUS_BIND).build();
                userOauthBindService.insert(oauthBind);
            }
            
            String token = jwtTokenManager.createTokenV2(user.getUserType(),
                    new TokenUser(user.getUid(), user.getPhone(), user.getName(), user.getUserType(), user.getDataType(), user.getTenantId()), System.currentTimeMillis());
            
            // 返回登录信息
            MerchantLoginVO merchantLoginVO = new MerchantLoginVO();
            merchantLoginVO.setPhone(user.getPhone());
            merchantLoginVO.setUsername(user.getName());
            merchantLoginVO.setToken(token);
            merchantLoginVO.setBindBusinessId(userBindBusinessDTO.getBindBusinessId());
            merchantLoginVO.setUid(user.getUid());
            merchantLoginVO.setUserType(user.getUserType());
            return Triple.of(true, null, merchantLoginVO);
            
        } finally {
            redisService.delete(CacheConstant.CAHCE_THIRD_OAHTH_KEY + merchantLoginRequest.getCode());
        }
        
    }
    
    private UserBindBusinessDTO checkUserBindingBusiness(User user) {
        UserBindBusinessDTO userBindBusinessDTO = new UserBindBusinessDTO();
        if (User.TYPE_USER_MERCHANT.equals(user.getUserType())) {
            Merchant merchant = merchantService.queryByUid(user.getUid());
            if (Objects.isNull(merchant)) {
                userBindBusinessDTO.setBinding(false);
            } else {
                userBindBusinessDTO.setBinding(true);
                userBindBusinessDTO.setBindBusinessId(merchant.getId());
                userBindBusinessDTO.setPurchaseAuthority(merchant.getPurchaseAuthority());
                userBindBusinessDTO.setEnterprisePackageAuth(merchant.getEnterprisePackageAuth());
            }
        } else if (User.TYPE_USER_CHANNEL.equals(user.getUserType())) {
            ChannelEmployeeVO channelEmployeeVO = channelEmployeeService.queryByUid(user.getUid());
            if (Objects.isNull(channelEmployeeVO)) {
                userBindBusinessDTO.setBinding(false);
            } else {
                userBindBusinessDTO.setBinding(true);
                userBindBusinessDTO.setBindBusinessId(channelEmployeeVO.getId());
                userBindBusinessDTO.setPurchaseAuthority(UserBindBusinessDTO.AUTHORITY_DISABLE);
                userBindBusinessDTO.setEnterprisePackageAuth(UserBindBusinessDTO.AUTHORITY_DISABLE);
            }
            
        } else {
            userBindBusinessDTO.setBinding(false);
        }
        return userBindBusinessDTO;
    }
}

@Data
class UserBindBusinessDTO {
    
    private boolean isBinding;
    
    /**
     * 绑定的商户/渠道ID
     */
    private Long bindBusinessId;
    
    /**
     * 站点代付权限：1-开启，0-关闭
     */
    private Integer enterprisePackageAuth;
    
    /**
     * 会员代付权限 0：关，1：开
     */
    private Integer purchaseAuthority;
    
    public static final Integer AUTHORITY_DISABLE = 0;
    
    
}
