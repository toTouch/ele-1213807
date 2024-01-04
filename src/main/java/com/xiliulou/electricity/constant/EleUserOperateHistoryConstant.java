package com.xiliulou.electricity.constant;

/**
 * Description: EleUserOperateHistoryConstant
 *
 * @author zhangyongbo
 * @version 1.0
 * @since 2023/12/28 15:15
 */
public class EleUserOperateHistoryConstant {
    
    /**
     * 操作用户相关记录
     */
    public static final Integer OPERATE_TYPE_USER = 1;
    
    
    /**
     * 操作用户账户相关记录
     */
    public static final Integer OPERATE_MODEL_USER_ACCOUNT = 1;
    
    
    /**
     * 操作内容 1：解绑微信
     */
    public static final Integer OPERATE_CONTENT_UNBIND_VX= 1;
    
    /**
     * 操作内容 2：解绑微信
     */
    public static final Integer OPERATE_CONTENT_UPDATE_PHONE= 2;
    
    public static final String UNBIND_VX_OLD_OPERATION = "綁定微信";
    public static final String UNBIND_VX_NEW_OPERATION = "解绑微信";
}