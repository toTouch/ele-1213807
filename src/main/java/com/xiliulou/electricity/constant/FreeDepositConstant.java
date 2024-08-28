package com.xiliulou.electricity.constant;

/**
 * @ClassName: FreeDepositConstant
 * @description:
 * @author: renhang
 * @create: 2024-08-23 23:11
 */
public class FreeDepositConstant {
    
    public static final String SUCCESS_CODE = "WZF00000";
    
    /**
     * 授权状态免押状态
     */
    public static final String FY_INIT = "INIT";
    
    public static final String FY_SUCCESS = "SUCCESS";
    
    public static final String FY_FAILED = "FAILED";
    
    public static final String FY_CLOSE = "CLOSED";
    
    /**
     * 代扣回调状态
     */
    public static final Integer AUTH_PXZ_SUCCESS_RECEIVE = 0;
    
    public static final String AUTH_PXZ_SUCCESS_RSP = "000000";
    public static final String AUTH_PXZ_FAIL_RSP = "000001";
    public static final String AUTH_FY_SUCCESS_RSP = "SUCCESS";
}
