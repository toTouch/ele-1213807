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
 * @author: Ant
 * @Date 2024/4/22
 * @Description: 技术内部使用接口的拦截器
 **/
@Slf4j
@Component
public class AdminSupperInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (ObjectUtils.isEmpty(userInfo)) {
            log.error("AdminSupperInterceptor error. not found user.");
            preHandleFalseResponseJsonBody(R.fail("ELECTRICITY.0001", "未找到用户"), response);
            return false;
        }
        
        if (1 != userInfo.getTenantId()) {
            log.error("AdminSupperInterceptor error. The use's tenant not admin.");
            preHandleFalseResponseJsonBody(R.fail("ELECTRICITY.0066", "用户权限不足"), response);
            return false;
        }
        
        return true;
    }
    
    private void preHandleFalseResponseJsonBody(R r, HttpServletResponse response) throws Exception {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.print(JsonUtil.toJson(r));
        writer.close();
        response.flushBuffer();
    }
}
