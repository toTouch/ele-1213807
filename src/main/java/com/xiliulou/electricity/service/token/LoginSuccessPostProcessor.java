package com.xiliulou.electricity.service.token;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.security.authentication.AuthenticationSuccessPostProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Service
public class LoginSuccessPostProcessor implements AuthenticationSuccessPostProcessor {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication, Integer type) throws UnknownHostException {
        log.info("authentication is --> {}",JsonUtil.toJson(authentication));
        InetAddress ip = InetAddress.getLocalHost();
        log.info("ip is -->{}",ip);
    }
}
