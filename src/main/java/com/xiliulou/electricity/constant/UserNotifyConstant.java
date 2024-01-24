package com.xiliulou.electricity.constant;

/**
 * Description: UserNotifyConstant
 *
 * @author zhangyongbo
 * @version 1.0
 * @since 2024/1/11
 */
public class UserNotifyConstant {
    
    /**
     * 兼容旧版小程序   旧版小程序未升级时，如果后台给设置为图片通知，则小程序通知框需要关闭
     */
    public static final Integer NEW_VERSION = 1;
    
    /**
     * 通知类型：0：文字  1：图片
     */
    public static final Integer TYPE_CONTENT = 0;
    
    /**
     * 通知类型：0：文字  1：图片
     */
    public static final Integer TYPE_PICTURE = 1;
    
    /**
     * 通知状态：0：打开  1：关闭
     */
    public static final Integer STATUS_OFF = 1;
    
    /**
     * 通知状态：0：打开  1：关闭
     */
    public static final Integer STATUS_ON = 0;
}
