package com.xiliulou.electricity.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author xiaohui.song
 **/
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 7560296618675661350L;

    /**
     * 错误编码
     */
    private String errCode;

    /**
     * 提示消息
     */
    private String errMsg;

    public BizException(String errMsg) {
        super(errMsg);
        this.errMsg = errMsg;
    }

    public BizException(String errMsg, Throwable e) {
        super(errMsg, e);
        this.errMsg = errMsg;
    }

    public BizException(String errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public BizException(String errCode, String errMsg, Throwable e) {
        super(errMsg, e);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public BizException setMessage(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }

    @Override
    public String getMessage() {
        return errMsg;
    }
}
