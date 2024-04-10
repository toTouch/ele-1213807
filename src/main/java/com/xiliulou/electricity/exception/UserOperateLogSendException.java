package com.xiliulou.electricity.exception;


/**
 * <p>
 * Description: This class is UserOperateLogSendException!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/26
 **/
public class UserOperateLogSendException extends RuntimeException{

    public UserOperateLogSendException() {
    }

    public UserOperateLogSendException(String message) {
        super(message);
    }

    public UserOperateLogSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserOperateLogSendException(Throwable cause) {
        super(cause);
    }

    public UserOperateLogSendException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
