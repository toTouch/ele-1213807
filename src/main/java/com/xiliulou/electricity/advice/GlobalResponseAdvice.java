package com.xiliulou.electricity.advice;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author xiaohui.song
 **/
@Slf4j
@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof R) {
            R result = (R) body;
            result.setTraceId(MDC.get(CommonConstant.TRACE_ID));
            return result;
        }
        return body;
    }
}
