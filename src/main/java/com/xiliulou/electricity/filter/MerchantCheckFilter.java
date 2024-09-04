package com.xiliulou.electricity.filter;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.utils.MerchantUserContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @author : eclair
 * @date : 2024/2/18 16:17
 */
@Slf4j
@Component("merchantCheckFilter")
@Order(14)
public class MerchantCheckFilter implements Filter {
    @Autowired
    UserService userService;

    @Autowired
    MerchantService merchantService;

    private RequestMatcher requiresAuthenticationRequestMatcher;

    public MerchantCheckFilter() {
        this.requiresAuthenticationRequestMatcher = new AntPathRequestMatcher("/merchant/**");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (!this.requiresAuthentication(httpServletRequest, response)) {
            filterChain.doFilter(httpServletRequest, response);
            return;
        }

        User user = userService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(user)) {
            log.warn("user not exists! uid={}", SecurityUtils.getUid());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ResponseUtil.out(response, R.fail("用户不存在"));
            return;
        }

        if (user.isLock()) {
            log.warn("user is locked! uid={}", SecurityUtils.getUid());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ResponseUtil.out(response, R.fail("当前登录账号已禁用，请联系客服处理"));
            return;
        }

        MerchantUserContextHolder.setUser(user);
        try {
            filterChain.doFilter(servletRequest, response);
        } finally {
            MerchantUserContextHolder.clear();
        }

    }

    private boolean requiresAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse response) {
        return this.requiresAuthenticationRequestMatcher.matches(httpServletRequest);
    }

}


