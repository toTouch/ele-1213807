package com.xiliulou.electricity.exception;

/**
 * @author : eclair
 * @date : 2022/12/29 09:33
 */
public class NotFoundServiceInstanceException extends RuntimeException {
    
    public NotFoundServiceInstanceException(String message) {
        super(message);
    }
}
