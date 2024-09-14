package com.xiliulou.electricity.filter;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.utils.ThirdMallConfigHolder;
import com.xiliulou.electricity.web.entity.BodyReaderHttpServletRequestWrapper;
import com.xiliulou.thirdmall.constant.meituan.virtualtrade.VirtualTradeConstant;
import com.xiliulou.thirdmall.entity.meituan.response.JsonR;
import com.xiliulou.thirdmall.enums.meituan.virtualtrade.VirtualTradeStatusEnum;
import com.xiliulou.thirdmall.util.meituan.MeiTuanRiderMallUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/12 13:49:11
 */

@Slf4j
@Component("thirdMallCheckFilter")
@Order(15)
public class ThirdMallCheckFilter implements Filter {
    
    @Resource
    private MeiTuanRiderMallConfigService meiTuanRiderMallConfigService;
    
    private RequestMatcher requiresAuthenticationRequestMatcher;
    
    
    public ThirdMallCheckFilter() {
        this.requiresAuthenticationRequestMatcher = new AntPathRequestMatcher("/outer/batteryMemberCard/**");
    }
    
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String header = httpServletRequest.getHeader(HttpHeaders.CONTENT_TYPE);
        
        if (!this.requiresAuthentication(httpServletRequest, response)) {
            filterChain.doFilter(httpServletRequest, response);
            return;
        }
        
        String params;
        if (StrUtil.isEmpty(header) || header.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE) || header.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            params = JsonUtil.toJson(httpServletRequest.getParameterMap());
            filterChain.doFilter(httpServletRequest, servletResponse);
        } else {
            httpServletRequest = new BodyReaderHttpServletRequestWrapper(httpServletRequest);
            
            if (header.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
                params = getRequestBody(httpServletRequest);
            } else {
                params = JsonUtil.toJson(httpServletRequest.getParameterMap());
            }
            
            filterChain.doFilter(httpServletRequest, servletResponse);
        }
        
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, Object> paramMap = JsonUtil.fromJson(params, type);
        String appId = null;
        String appKey = null;
        String sign = null;
        
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (Objects.nonNull(value)) {
                if (Objects.equals(VirtualTradeConstant.APP_ID, key)) {
                    appId = value.toString();
                } else if (Objects.equals(VirtualTradeConstant.APP_KEY, key)) {
                    appKey = value.toString();
                } else if (Objects.equals(VirtualTradeConstant.SIGN, key)) {
                    sign = value.toString();
                }
            }
        }
        
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(appKey) || StringUtils.isBlank(sign)) {
            log.error("ThirdMallCheckFilter error! appId={}, appKey={}, sign={}", appId, appKey, sign);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            this.out(response, JsonR.fail(VirtualTradeStatusEnum.FAIL_APP_CONFIG.getCode(), VirtualTradeStatusEnum.FAIL_APP_CONFIG.getDesc()));
            return;
        }
        
        MeiTuanRiderMallConfig meiTuanRiderMallConfig = meiTuanRiderMallConfigService.queryByConfigFromCache(MeiTuanRiderMallConfig.builder().appId(appId).appKey(appKey).build());
        log.info("ThirdMallCheckFilter info! meiTuanRiderMallConfig={}", meiTuanRiderMallConfig);
        if (Objects.isNull(meiTuanRiderMallConfig)) {
            log.error("ThirdMallCheckFilter error! meiTuanRiderMallConfig is null, appId={}, appKey={}, sign={}", appId, appKey, sign);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            this.out(response, JsonR.fail(VirtualTradeStatusEnum.FAIL_APP_CONFIG.getCode(), VirtualTradeStatusEnum.FAIL_APP_CONFIG.getDesc()));
            return;
        }
        
        Boolean checkSign = MeiTuanRiderMallUtil.checkSign(paramMap, meiTuanRiderMallConfig.getSecret(), sign);
        if (!checkSign) {
            log.error("ThirdMallCheckFilter error! checkSign fail, appId={}, appKey={}, sign={}", appId, appKey, sign);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            this.out(response, JsonR.fail(VirtualTradeStatusEnum.FAIL_CHECK_SIGN.getCode(), VirtualTradeStatusEnum.FAIL_CHECK_SIGN.getDesc()));
            return;
        }
        
        ThirdMallConfigHolder.setTenantId(meiTuanRiderMallConfig.getTenantId());
        filterChain.doFilter(servletRequest, response);
    }
    
    private String getRequestBody(HttpServletRequest request) {
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return "";
        }
        try {
            return IOUtils.toString(request.getReader());
        } catch (IOException e) {
            log.error("获取请求体失败", e);
            return "";
        }
    }
    
    private void out(HttpServletResponse response, JsonR r) {
        ObjectMapper mapper = new ObjectMapper();
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            mapper.writeValue(response.getWriter(), r);
        } catch (IOException var4) {
            log.error("");
        }
        
    }
    
    private boolean requiresAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse response) {
        return this.requiresAuthenticationRequestMatcher.matches(httpServletRequest);
    }
    
}
