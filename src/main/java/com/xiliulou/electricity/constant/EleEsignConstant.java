package com.xiliulou.electricity.constant;

/**
 * @author: Kenneth
 * @Date: 2023/7/7 23:40
 * @Description:
 */
public class EleEsignConstant {
    public static final Integer DEL_NO = 0;

    public static final Integer DEL_YES = 1;

    public static final Integer ESIGN_ENABLE = 0;

    public static final Integer ESIGN_DISABLE = 1;

    public static final Integer ESIGN_TURN_OFF = 2;

    /**
     * 签署流程完成
     */
    public static final Integer ESIGN_FLOW_STATUS_COMPLETE = 2;

    /**
     * 签署状态已完成
     */
    public static final Integer ESIGN_STATUS_SUCCESS = 1;

    /**
     * 签署状态未完成
     */
    public static final Integer ESIGN_STATUS_FAILED = 0;

    public static final Integer ESIGN_RESPONSE_SUCCESS_CODE = 0;

    public static final Integer ESIGN_REAL_NAME_STATUS_Y = 1;

    public static final Integer ESIGN_REAL_NAME_STATUS_N = 0;

    public static final Integer ESIGN_MIN_CAPACITY = 0;

    /**
     * 签署流程有效期为90天，判断时将时间提前一天
     */
    public static final Integer ESIGN_FLOW_EXPIRED_DATE = 89;

    public static final String PSN_PHOTO_OSS_PATH = "saas/psnPhoto/";

}
