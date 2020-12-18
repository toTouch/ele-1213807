package com.xiliulou.electricity.filter;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.web.entity.BodyReaderHttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

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
 * @Auther: eclair
 * @Date: 2019/11/4 09:07
 * @Description:
 */
@Slf4j
@Component("newRequestInterceptor")
public class RequestFilter implements Filter {

    private static final String REQ_TIME = "req_time";

    public void afterCompletion(HttpServletRequest request) {
        Object attribute = request.getAttribute(REQ_TIME);
        if (Objects.isNull(attribute) || StrUtil.isEmpty(attribute.toString())) {
            return;
        }
        Long startTime = null;
        try {
            startTime = Long.parseLong(attribute.toString());
        } catch (Exception e) {
            log.error("parse startTime error!param={}", attribute, e);
        }
        if (Objects.isNull(startTime)) {
            return;
        }
        Long uid = SecurityUtils.getUid();
        log.info("uid={},method={},uri={},time={}", uid, request.getMethod(), request.getRequestURI(), System.currentTimeMillis() - startTime);

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

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String header = httpServletRequest.getHeader(HttpHeaders.CONTENT_TYPE);

        if (Objects.isNull(httpServletRequest.getAttribute(REQ_TIME)) || StrUtil.isEmpty(httpServletRequest.getAttribute(REQ_TIME).toString())) {
            httpServletRequest.setAttribute(REQ_TIME, System.currentTimeMillis());
        }

        Long uid = SecurityUtils.getUid();

        if (StrUtil.isEmpty(header) || header.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            log.info("uid={},method={},uri={},params={}", uid, httpServletRequest.getMethod(), httpServletRequest.getRequestURI(), JsonUtil.toJson(httpServletRequest.getParameterMap()));
            filterChain.doFilter(httpServletRequest, servletResponse);
        }  else {
            httpServletRequest = new BodyReaderHttpServletRequestWrapper(httpServletRequest);

            if (header.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
                log.info("uid={},method={},uri={},params={}", uid, httpServletRequest.getMethod(), httpServletRequest.getRequestURI(), getRequestBody(httpServletRequest));
            } else {
                log.info("uid={},method={},uri={},params={}", uid, httpServletRequest.getMethod(), httpServletRequest.getRequestURI(), JsonUtil.toJson(httpServletRequest.getParameterMap()));
            }

            filterChain.doFilter(httpServletRequest, servletResponse);

        }
        afterCompletion(httpServletRequest);


    }
}
