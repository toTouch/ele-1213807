package com.xiliulou.electricity.filter;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.WebUtils;
import com.xiliulou.electricity.web.entity.BodyReaderHttpServletRequestWrapper;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.*;
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
@Order(10)
public class RequestFilter implements Filter {
    
    @Autowired
    CollectorRegistry collectorRegistry;
    
    public static final String REQUEST_ID = "requestId";
    
    private static final String REQ_TIME = "req_time";
    
    private static final String REQ_HISTOGRAM_TIMER = "req_histogram_time";
    
    static final Gauge PROGRESSING_REQUESTS = Gauge.build().name("progressing_requests").help("Inprogress requesets.")
            .register();
    
    static final Gauge REQUEST_LATENCY_GAUGE = Gauge.build().name("request_latency_gauge")
            .labelNames("path", "method", "code").help("request_latency_gauge").register();
    
    static final Histogram REQUEST_LATENCY_HISTOGRAM = Histogram.build().labelNames("path", "method", "code")
            .buckets(0.2, 0.5, 0.75, 0.9, 1, 3, 10).name("http_request_latency_histogram")
            .help("Request latency in seconds.").register();
    
    @PostConstruct
    public void init() {
        collectorRegistry.register(PROGRESSING_REQUESTS);
        collectorRegistry.register(REQUEST_LATENCY_HISTOGRAM);
        collectorRegistry.register(REQUEST_LATENCY_GAUGE);
    }
    
    public void afterCompletion(String ip, HttpServletRequest request, String params,
            HttpServletResponse httpServletResponse) {
        PROGRESSING_REQUESTS.dec();
        
        Object attribute = request.getAttribute(REQ_TIME);
        Object timerObj = request.getAttribute(REQ_HISTOGRAM_TIMER);
        if (Objects.isNull(attribute) || StrUtil.isEmpty(attribute.toString())) {
            return;
        }
        
        if (Objects.isNull(timerObj)) {
            return;
        }
        
        Histogram.Timer timer = (Histogram.Timer) timerObj;
        timer.observeDuration();
        
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
        long spendTimeMills = System.currentTimeMillis() - startTime;
        
        REQUEST_LATENCY_GAUGE.labels(request.getRequestURI(),
                request.getMethod(), String.valueOf(httpServletResponse.getStatus())).set(spendTimeMills);
        log.info("ip={} uid={} method={} uri={} params={} time={}", ip, uid, request.getMethod(),
                request.getRequestURI(), params, spendTimeMills);

        
        
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
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String header = httpServletRequest.getHeader(HttpHeaders.CONTENT_TYPE);
        
        PROGRESSING_REQUESTS.inc();
        Histogram.Timer requestTimer = REQUEST_LATENCY_HISTOGRAM.labels(httpServletRequest.getRequestURI(),
                httpServletRequest.getMethod(), String.valueOf(httpServletResponse.getStatus())).startTimer();
        
        if (Objects.isNull(httpServletRequest.getAttribute(REQ_TIME)) || StrUtil.isEmpty(
                httpServletRequest.getAttribute(REQ_TIME).toString())) {
            httpServletRequest.setAttribute(REQ_TIME, System.currentTimeMillis());
            httpServletRequest.setAttribute(REQ_HISTOGRAM_TIMER, requestTimer);
        }
        
        String ip = WebUtils.getIP(httpServletRequest);
        String requestId = IdUtil.simpleUUID();
        
        httpServletRequest.setAttribute(REQUEST_ID, requestId);
        
        String params = null;
        try {
            if (StrUtil.isEmpty(header) || header.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE) || header.startsWith(
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
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
        }finally {
            afterCompletion(ip, httpServletRequest, params,httpServletResponse);
    
        }
       
        
    }
}
