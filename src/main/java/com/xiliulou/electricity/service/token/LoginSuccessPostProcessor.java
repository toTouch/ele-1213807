package com.xiliulou.electricity.service.token;


import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.security.authentication.AuthenticationSuccessPostProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
public class LoginSuccessPostProcessor implements AuthenticationSuccessPostProcessor {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication, Integer type) {
        log.info(JsonUtil.toJson(authentication));
        if (request.getHeader("x-forwarded-for") == null) {
            log.info(request.getRemoteAddr());
        }else {
            log.info(request.getHeader("x-forwarded-for"));
        }
    }
}
