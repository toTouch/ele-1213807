package com.xiliulou.electricity.constant;

/**
 * 商户常量类
 *
 * @author zzlong
 * @since 2024-02-04 09:14:33
 */
public interface MerchantConstant {
    /**
     * 状态：启用
     */
    public final static Integer ENABLE = 0;
    /**
     * 状态：禁用
     */
    public final static Integer DISABLE = 1;
    
    /**
     * 商户自动升级：开启
     */
    public final static Integer open = 0;
    /**
     * 商户自动升级：关闭
     */
    public final static Integer close = 1;
    
    /**
     * 邀请权限：0-开启
     */
    public final static Integer INVITE_AUTH_OPEN = 0;
    /**
     * 邀请权限：1-关闭
     */
    public final static Integer INVITE_AUTH_CLOSE = 1;
    
    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
