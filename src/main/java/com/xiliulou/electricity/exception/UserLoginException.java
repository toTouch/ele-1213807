package com.xiliulou.electricity.exception;

import lombok.Getter;

/**
 * Description: UserLoginException
 *
 * @author zhangyongbo
 * @version 1.0
 * @since 2024/1/5 14:15
 */
@Getter
public class UserLoginException extends RuntimeException  {
    /**
     * 错误编码
     */
    private String errCode;
    
    /**
     * 提示消息
     */
    private String errMsg;
    
    public UserLoginException(String errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
    
    public UserLoginException(String errCode, String errMsg, Throwable e) {
        super(errMsg, e);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
}