package com.xiliulou.electricity.service.retrofit.interceptor;


import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;
import com.xiliulou.electricity.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Description: This class is MeituanServiceInterceptor!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/10/12
 **/
@Slf4j
@Component
public class MeituanServiceInterceptor extends BasePathMatchInterceptor {
    
    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder().addHeader(CommonConstant.TRACE_ID, MDC.get(CommonConstant.TRACE_ID));
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(requestAttributes)) {
            return chain.proceed(builder.build());
        }
        HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        Enumeration<String> headerNames = servletRequest.getHeaderNames();
        if (Objects.isNull(servletRequest.getHeaderNames())) {
            return chain.proceed(builder.build());
        }
        Map<String, String> headers = new HashMap<>();
        headerNames.asIterator().forEachRemaining(headerName -> {
            headers.put(headerName, servletRequest.getHeader(headerName));
        });
        builder.headers(Headers.of(headers));
        return chain.proceed(builder.build());
    }
}
