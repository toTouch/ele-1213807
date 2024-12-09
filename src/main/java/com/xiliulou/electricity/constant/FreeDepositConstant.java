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
    
    public static final String AUTH_FY_SUCCESS_RSP = "SUCCESS";
    
    
    /**
     * 代扣状态：
     * 状态【0：待处理；1：成功；2：退款】
     */
    public static final Integer FY_AUTH_STATUS_INIT = 0;
    
    public static final Integer FY_AUTH_STATUS_SUCCESS =1;
    
    public static final Integer FY_AUTH_STATUS_FAIL = 2;
    
    /**
     * 免押混合支付传递了改参数并且值为1，才能灵活续费
     */
    public static final Integer FLEXIBLE_RENEWAL = 1;
}
