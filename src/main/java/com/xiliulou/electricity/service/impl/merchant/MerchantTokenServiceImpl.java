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
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.query.merchant.MerchantLoginRequest;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
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
import io.jsonwebtoken.lang.Collections;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
            String codeUrl = String.format(CacheConstant.WX_MIN_PRO_AUTHORIZATION_CODE_URL, "wxc2dd558f2ee2ab8a",
                    "b029fdccc213ae48c81e4243d4f2e1ef", merchantLoginRequest.getCode());

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

            List<User> users = userService.listUserByPhone(purePhoneNumber, tenantId);
            if (Collections.isEmpty(users) || !(users.stream().filter(user -> User.TYPE_USER_MERCHANT.equals(user.getUserType()) || User.TYPE_USER_CHANNEL.equals(user.getUserType())).count() > 1)) {
                return Triple.of(false, null, "用户不存在");
            }

            List<User> notLockUsers = users.stream().filter(user -> !user.isLock()).collect(Collectors.toList());
            if (notLockUsers.isEmpty()) {
                return Triple.of(false, null, "当前登录账号已禁用，请联系客服处理");
            }

            // 用户是否绑定了业务信息
            Map<Long, UserBindBusinessDTO> userBindBusinessDTOS = users.stream().map(this::checkUserBindingBusiness).filter(e -> !e.isBinding()).collect(Collectors.toMap(UserBindBusinessDTO::getUid, e -> e));
            if (userBindBusinessDTOS.isEmpty()) {
                return Triple.of(false, null, "未找到绑定账号，请检查");
            }

            List<MerchantLoginVO> loginVOS = notLockUsers.parallelStream().map(e -> {
                // 查看是否有绑定的第三方信息,如果没有绑定创建一个
                if (!wxProThirdAuthenticationService.checkOpenIdExists(result.getOpenid(), tenantId).getLeft()) {
                    UserOauthBind oauthBind = UserOauthBind.builder().createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                            .phone(wxMinProPhoneResultDTO.getPurePhoneNumber()).uid(e.getUid()).accessToken("").refreshToken("").thirdNick("").tenantId(tenantId)
                            .thirdId(result.getOpenid()).source(UserOauthBind.SOURCE_WX_PRO).status(UserOauthBind.STATUS_BIND).build();
                    userOauthBindService.insert(oauthBind);
                }
                String token = jwtTokenManager.createTokenV2(e.getUserType(),
                        new TokenUser(e.getUid(), e.getPhone(), e.getName(), e.getUserType(), e.getDataType(), e.getTenantId()), System.currentTimeMillis());

                MerchantLoginVO merchantLoginVO = new MerchantLoginVO();
                merchantLoginVO.setPhone(e.getPhone());
                merchantLoginVO.setUsername(e.getName());
                merchantLoginVO.setToken(token);
                merchantLoginVO.setBindBusinessId(userBindBusinessDTOS.get(e.getUid()).getBindBusinessId());
                merchantLoginVO.setUid(e.getUid());
                merchantLoginVO.setUserType(e.getUserType());
                merchantLoginVO.setBusinessInfo(userBindBusinessDTOS.get(e.getUid()).getEnterprisePackageAuth(), userBindBusinessDTOS.get(e.getUid()).getEnterprisePackageAuth());
                return merchantLoginVO;
            }).collect(Collectors.toList());
            return Triple.of(true, null, loginVOS);
            
        } finally {
            redisService.delete(CacheConstant.CAHCE_THIRD_OAHTH_KEY + merchantLoginRequest.getCode());
        }
        
    }
    
    private UserBindBusinessDTO checkUserBindingBusiness(User user) {
        UserBindBusinessDTO userBindBusinessDTO = new UserBindBusinessDTO();
        userBindBusinessDTO.setUid(user.getUid());
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

    private Long uid;
    
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
