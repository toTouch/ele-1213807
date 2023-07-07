package com.xiliulou.electricity.advice;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.exception.BizException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaohui.song
 **/
@Slf4j
@RestControllerAdvice
public class SaaSGlobalExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(BizException.class)
    public R handlerBizException(BizException e) {
        log.error("全局异常拦截业务 BizException error: ", e);
        return R.fail(e.getErrCode(), e.getMessage());
    }

}
