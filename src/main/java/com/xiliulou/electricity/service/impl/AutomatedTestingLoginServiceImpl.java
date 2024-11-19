/**
 *  Create date: 2024/7/4
 */

package com.xiliulou.electricity.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.aliyuncs.auth.AuthConstant;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.query.merchant.AutomatedTestingLoginRequest;
import com.xiliulou.electricity.service.AutomatedTestingLoginService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.security.authentication.JwtTokenManager;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import com.xiliulou.security.bean.SecurityUser;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.security.constant.TokenConstant;
import com.xiliulou.security.utils.ResponseUtil;
import com.xiliulou.security.web.SecurityUserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/4 17:46
 */
@Slf4j
@Service
public class AutomatedTestingLoginServiceImpl implements AutomatedTestingLoginService {
    
    
    @Autowired
    CustomPasswordEncoder customPasswordEncoder;
    
    @Autowired
    private UserService userService;
    
    

    @Autowired
    private JwtTokenManager jwtTokenManager;
    
   static String ENCODE_PASSWORD_KEY = "xiliu&lo@u%12345";
    
    
    @Override
    public Triple<Boolean, String, Object> login(HttpServletRequest request, AutomatedTestingLoginRequest loginRequest) {
        
        String clientId = request.getHeader(TokenConstant.SINGLE_HEADER_TOKEN_CLIENT_ID_KEY);
    
        Integer tenantId = loginRequest.getTenantId();
        
        User user = userService.queryByUserPhoneFromDB(loginRequest.getUserPhone(), User.TYPE_USER_NORMAL_WX_PRO, tenantId);
        
        if (Objects.isNull(user)){
            throw new AuthenticationServiceException("手机号不存在");
        }
        
        if (!customPasswordEncoder.matches(this.decryptPassword(loginRequest.getPassword()), user.getLoginPwd())) {
            throw new AuthenticationServiceException("登录信息异常，请联系客服处理");
        }
    
        String token = jwtTokenManager
                .createToken(clientId, user.getUserType(), new TokenUser(user.getUid(), user.getPhone(), user.getName(), user.getUserType(), user.getTenantId()),
                        System.currentTimeMillis());
        
        SecurityUserVo vo = SecurityUserVo.builder().phone(user.getPhone()).uid(user.getUid()).username(user.getName()).userType(user.getUserType()).token(token)
                .tenantId(user.getTenantId()).build();
        return Triple.of(true, null, vo);
    }
    
    
    public  String decryptPassword(String encryptPassword) {
        AES aes = new AES(Mode.CBC, Padding.ZeroPadding, new SecretKeySpec(ENCODE_PASSWORD_KEY.getBytes(), "AES"), new IvParameterSpec(ENCODE_PASSWORD_KEY.getBytes()));
        return new String(aes.decrypt(Base64.decode(encryptPassword.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
    }
}
