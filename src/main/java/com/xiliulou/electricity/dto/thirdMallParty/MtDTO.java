package com.xiliulou.electricity.dto.thirdMallParty;

import lombok.Data;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/4 19:53:47
 */
@Data
public class MtDTO<T> {
    
    public static final String USE_FAILED = "THIRD_MALL.0026";
    
    public static final String CANCEL_FAILED = "THIRD_MALL.0027";
    private static final Integer SUCCESS = 0;
    
    private static final Integer FAIL = 1;
    
    private static final String MSG_SUCCESS = "success";
    
    private String traceId;
    
    private Integer code;
    
    private String errCode;
    
    private String msg;
    
    private T data;
    
    public static <T> MtDTO<T> ok() {
        return returnResult(SUCCESS, MSG_SUCCESS);
    }
    
    public static <T> MtDTO<T> ok(T data) {
        return returnResult(SUCCESS, MSG_SUCCESS, data);
    }
    
    public static <T> MtDTO<T> fail(String msg) {
        return returnResult(FAIL, msg);
    }
    
    public static <T> MtDTO<T> fail(String msg, T data) {
        return returnResult(FAIL, msg, data);
    }
    
    public static <T> MtDTO<T> failError(String errCode) {
        MtDTO<T> apiResult = new MtDTO<>();
        apiResult.code = FAIL;
        apiResult.errCode = errCode;
        return apiResult;
    }
    public static <T> MtDTO<T> failMsg(String msg) {
        return fail(msg);
    }
    
    private static <T> MtDTO<T> returnResult(Integer code, String msg, T data) {
        MtDTO<T> apiResult = returnResult(code, msg);
        apiResult.data = data;
        return apiResult;
    }
    
    private static <T> MtDTO<T> returnResult(Integer code, String msg) {
        MtDTO<T> apiResult = new MtDTO<>();
        apiResult.code = code;
        apiResult.msg = msg;
        return apiResult;
    }
    
    public boolean isSuccess(){
        return SUCCESS.equals(code);
    }
}
