package com.xiliulou.electricity.interceptor;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @author: maxiaodong
 * @Date 2024/4/24
 * @Description: 企业套餐拦截器
 **/
@Slf4j
@Component
public class EnterprisePackageInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (ObjectUtils.isEmpty(userInfo)) {
            log.error("EnterprisePackageInterceptor error. not found user.");
            preHandleFalseResponseJsonBody(R.fail("ELECTRICITY.0001", "未找到用户"), response);
            return false;
        }
    
        preHandleFalseResponseJsonBody(R.fail("120238", "企业渠道全新升级，请点击前往商户版使用"), response);
        return false;
    }
    
    private void preHandleFalseResponseJsonBody(R r, HttpServletResponse response) throws Exception {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.print(JsonUtil.toJson(r));
        writer.close();
        response.flushBuffer();
    }
}
