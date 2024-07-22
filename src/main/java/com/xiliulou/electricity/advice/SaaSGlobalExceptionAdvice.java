package com.xiliulou.electricity.advice;

import com.xiliulou.core.exception.GlobalExceptionHandlerResolver;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.feishu.FeishuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author xiaohui.song
 **/
@Slf4j
@RestControllerAdvice
public class SaaSGlobalExceptionAdvice extends GlobalExceptionHandlerResolver {

    @Resource
    private FeishuService feishuService;

    @ResponseBody
    @ExceptionHandler(BizException.class)
    public R handlerBizException(HttpServletRequest request, BizException e) {
        log.warn("BizException warn: ", e);
        return R.fail(StringUtils.isBlank(e.getErrCode()) ? "000001" : e.getErrCode(), e.getMessage());
    }

    //与父类中的ExceptionHandler(Exception.class) 冲突，需要移除
    /*@ResponseBody
    @ExceptionHandler(Exception.class)
    public R handlerException(HttpServletRequest request, Exception e) {
        log.error("Exception error: ", e);
        feishuService.sendException(request.getRequestURI(), MDC.get(CommonConstant.TRACE_ID), e);
        return R.fail("000001", "系统异常");
    }*/

}
