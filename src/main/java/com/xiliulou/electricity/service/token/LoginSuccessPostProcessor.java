package com.xiliulou.electricity.service.token;


import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.LoginInfo;
import com.xiliulou.electricity.service.LoginInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.security.authentication.AuthenticationSuccessPostProcessor;
import com.xiliulou.security.bean.SecurityUser;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
public class LoginSuccessPostProcessor implements AuthenticationSuccessPostProcessor {
    private final String UNKNOWN = "unknown";
    @Autowired
    LoginInfoService loginInfoService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, SecurityUser user, Integer type) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        LoginInfo loginInfo = new LoginInfo();
        String ip = getIP(request);
        loginInfo.setIp(ip);
        loginInfo.setUid(user.getUid());
        loginInfo.setPhone(user.getPhone());
        loginInfo.setType(type);
        loginInfo.setLoginTime(System.currentTimeMillis());
        loginInfo.setTenantId(tenantId);
        loginInfoService.insert(loginInfo);
    }

    /**
     * 获取ip
     *
     * @param request HttpServletRequest
     * @return {String}
     */
    public String getIP(HttpServletRequest request) {
        Assert.notNull(request, "HttpServletRequest is null");
        String ip = request.getHeader("X-Requested-For");
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return StringUtils.isBlank(ip) ? null : ip.split(",")[0];
    }
}
