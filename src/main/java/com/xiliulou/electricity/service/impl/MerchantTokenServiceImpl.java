package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.merchant.MerchantConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.dto.WXMinProAuth2SessionResult;
import com.xiliulou.electricity.dto.WXMinProPhoneResultDTO;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.query.merchant.MerchantLoginRequest;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.MerchantTokenService;
import com.xiliulou.electricity.service.token.WxProThirdAuthenticationServiceImpl;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeeVO;
import com.xiliulou.electricity.vo.merchant.MerchantLoginVO;
import com.xiliulou.security.authentication.JwtTokenManager;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.security.constant.TokenConstant;
import io.jsonwebtoken.lang.Collections;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author : eclair
 * @date : 2024/2/18 10:19
 */
@Service
@Slf4j
public class MerchantTokenServiceImpl implements MerchantTokenService {
    
    @Resource
    private MerchantConfig merchantConfig;
    
    @Resource
    private TenantService tenantService;
    
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
    public Triple<Boolean, String, Object> login(HttpServletRequest request, MerchantLoginRequest merchantLoginRequest) {
        String clientId = request.getHeader(TokenConstant.SINGLE_HEADER_TOKEN_CLIENT_ID_KEY);
        if (StrUtil.isEmpty(clientId)) {
            log.error("merchant login error. not found client");
            throw new IllegalArgumentException("缺少xll-sin-client-id请求头");
        }
        
        if (!redisService.setNx(CacheConstant.CAHCE_THIRD_OAHTH_KEY + merchantLoginRequest.getCode(), "1", 5000L, false)) {
            return Triple.of(false, null, "操作频繁,请稍后再试");
        }
        
        
        long now = System.currentTimeMillis();
        
        try {
            String codeUrl = String.format(CacheConstant.WX_MIN_PRO_AUTHORIZATION_CODE_URL, merchantConfig.getMerchantAppletId(),
                    merchantConfig.getMerchantAppletSecret(), merchantLoginRequest.getCode());
            
            String bodyStr = restTemplateService.getForString(codeUrl, null);
            log.info("call wxpro get openId message is {}", bodyStr);
            
            WXMinProAuth2SessionResult result = JsonUtil.fromJson(bodyStr, WXMinProAuth2SessionResult.class);
            if (Objects.isNull(result) || StrUtil.isEmpty(result.getOpenid()) || StrUtil.isEmpty(result.getSession_key())) {
                log.error("merchant login error. wxResult has error! bodyStr={}", bodyStr);
                return Triple.of(false, null, "微信返回异常！");
            }
            
            // 解析手机号
            String s = wxProThirdAuthenticationService.decryptWxData(merchantLoginRequest.getData(), merchantLoginRequest.getIv(), result.getSession_key());
            if (StrUtil.isEmpty(s)) {
                return Triple.of(false, null, "解析微信数据失败");
            }
            
            WXMinProPhoneResultDTO wxMinProPhoneResultDTO = JsonUtil.fromJson(s, WXMinProPhoneResultDTO.class);
            if (Objects.isNull(wxMinProPhoneResultDTO) || StrUtil.isEmpty(wxMinProPhoneResultDTO.getPurePhoneNumber())) {
                log.error("merchant login error. 反序列化微信的手机号数据失败！s is {}", s);
                return Triple.of(false, null, "解析微信数据失败");
            }
            
            String purePhoneNumber = wxMinProPhoneResultDTO.getPurePhoneNumber();
            log.info("TOKEN INFO! 解析微信手机号:{}", purePhoneNumber);
            
            List<User> users = Optional.ofNullable(userService.listUserByPhone(purePhoneNumber)).orElse(Lists.newArrayList()).stream()
                    .filter(e -> (e.getUserType().equals(User.TYPE_USER_MERCHANT) || e.getUserType().equals(User.TYPE_USER_CHANNEL))).collect(Collectors.toList());
            
            if (Collections.isEmpty(users)) {
                return Triple.of(false, null, "未找到绑定账号，请检查");
            }
            
            List<User> notLockUsers = users.stream().filter(user -> !user.isLock()).collect(Collectors.toList());
            if (notLockUsers.isEmpty()) {
                return Triple.of(false, null, "当前登录账号已禁用，请联系客服处理");
            }
            
            // 用户是否绑定了业务信息
            Map<Long, UserBindBusinessDTO> userBindBusinessDTOS = users.stream().map(this::checkUserBindingBusiness).filter(UserBindBusinessDTO::isBinding)
                    .collect(Collectors.toMap(UserBindBusinessDTO::getUid, e -> e));
            if (userBindBusinessDTOS.isEmpty()) {
                return Triple.of(false, null, "未找到绑定账号，请检查");
            }
            
            log.info("userBindBusinessDTOS:{} notLockerUser:{}", userBindBusinessDTOS, notLockUsers);
            
            List<User> merchantUser = users.stream().filter((user -> User.TYPE_USER_MERCHANT.equals(user.getUserType()) || User.TYPE_USER_CHANNEL.equals(user.getUserType())))
                    .collect(Collectors.toList());
            
            String openid = result.getOpenid();
            List<MerchantLoginVO> loginVOS = merchantUser.stream().map(e -> {
                Tenant tenant = tenantService.queryByIdFromCache(e.getTenantId());
                if (ObjectUtils.isEmpty(tenant) || Tenant.STA_OUT.equals(tenant.getStatus()) || tenant.getExpireTime() <= now) {
                    log.warn("merchant login skip. The tenant info warn. tenant_id is {}", e.getTenantId());
                    return null;
                }
                
                if (Objects.isNull(userBindBusinessDTOS.get(e.getUid()))) {
                    return null;
                }
                
                Integer tenantId = tenant.getId();
                
                // 查看是否有绑定的第三方信息,如果没有绑定创建一个
                Pair<Boolean, List<UserOauthBind>> thirdOauthBindList = this.checkOpenIdExists(openid, tenantId);
                if (!thirdOauthBindList.getLeft()) {
                    List<UserOauthBind> userOauthBindByUidList = userOauthBindService.queryListByUid(e.getUid());
                    if (CollectionUtils.isNotEmpty(userOauthBindByUidList) && userOauthBindByUidList.stream().anyMatch(n -> !openid.equals(n.getThirdId()))) {
                        log.warn("merchant token login warning. the uid is bind other third id. uid is {}", e.getUid());
                        throw new CustomBusinessException("该账号已绑定过微信，无法直接登录，如需使用该微信登录，请先联系客服解除绑定");
                    }
                    
                    UserOauthBind oauthBind = UserOauthBind.builder().createTime(now).updateTime(now).phone(wxMinProPhoneResultDTO.getPurePhoneNumber()).uid(e.getUid())
                            .accessToken("").refreshToken("").thirdNick("").tenantId(tenantId).thirdId(openid).source(UserOauthBind.SOURCE_WX_PRO)
                            .status(UserOauthBind.STATUS_BIND).build();
                    userOauthBindService.insert(oauthBind);
                } else {
                    UserOauthBind userOauthBind = userOauthBindService.queryByUidAndTenantAndSource(e.getUid(), tenantId,UserOauthBind.SOURCE_WX_PRO);
                    
                    if (ObjectUtils.isNotEmpty(userOauthBind) && !openid.equals(userOauthBind.getThirdId())) {
                        log.warn("merchant token login warning. the uid is bind other third id. uid is {}", e.getUid());
                        throw new CustomBusinessException("该账号已绑定过微信，无法直接登录，如需使用该微信登录，请先联系客服解除绑定");
                    }
                    
                    if (ObjectUtils.isEmpty(userOauthBind)) {
                        // 同一个 open_id 绑定多个账号
                        UserOauthBind oauthBind = UserOauthBind.builder().createTime(now).updateTime(now).phone(wxMinProPhoneResultDTO.getPurePhoneNumber()).uid(e.getUid())
                                .accessToken("").refreshToken("").thirdNick("").tenantId(tenantId).thirdId(openid).source(UserOauthBind.SOURCE_WX_PRO)
                                .status(UserOauthBind.STATUS_BIND).build();
                        userOauthBindService.insert(oauthBind);
                    }
                }
                
                // 创建 token 为了保证只有一个登录，入参拼接 open_id + UID
                String token = jwtTokenManager.createToken(clientId, e.getUserType(),
                        new TokenUser(e.getUid(), e.getPhone(), openid + e.getUid(), e.getUserType(), e.getDataType(), e.getTenantId()), now);
                
                MerchantLoginVO merchantLoginVO = new MerchantLoginVO();
                merchantLoginVO.setPhone(e.getPhone());
                merchantLoginVO.setUsername(e.getName());
                merchantLoginVO.setToken(token);
                merchantLoginVO.setBindBusinessId(userBindBusinessDTOS.get(e.getUid()).getBindBusinessId());
                merchantLoginVO.setUid(e.getUid());
                merchantLoginVO.setUserType(e.getUserType());
                merchantLoginVO.setBusinessInfo(userBindBusinessDTOS.get(e.getUid()).getEnterprisePackageAuth(), userBindBusinessDTOS.get(e.getUid()).getEnterprisePackageAuth());
                merchantLoginVO.setLockFlag(e.getLockFlag());
                merchantLoginVO.setTenantId(tenantId.longValue());
                merchantLoginVO.setTenantName(tenant.getName());
                merchantLoginVO.setTenantCode(tenant.getCode());
                merchantLoginVO.setServicePhone(redisService.get(CacheConstant.CACHE_SERVICE_PHONE + tenantId));
                return merchantLoginVO;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return Triple.of(true, null, loginVOS);
            
        } finally {
            redisService.delete(CacheConstant.CAHCE_THIRD_OAHTH_KEY + merchantLoginRequest.getCode());
        }
        
    }
    
    private Pair<Boolean, List<UserOauthBind>> checkOpenIdExists(String openid, Integer tenantId) {
        List<UserOauthBind> userOauthBindList = userOauthBindService.selectListOauthByOpenIdAndSource(openid,
                UserOauthBind.SOURCE_WX_PRO, tenantId);
        return CollectionUtils.isNotEmpty(userOauthBindList) ? Pair.of(true, userOauthBindList) : Pair.of(false, null);
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
