package com.xiliulou.electricity.constant.merchant;

/**
 * @author maxiaodong
 * @date 2024/2/20 14:52
 * @desc
 */
public class RebateRecordConstant {
    /**
     * 结算状态 未结算
     */
    public static final Integer NOT_SETTLE = 0;
    
    /**
     * 结算状态 已结算
     */
    public static final Integer SETTLE = 1;
    
    /**
     * 结算状态 已退回（1-已结算,2-已退回,3-已失效）
     */
    public static final Integer RETURNED = 2;
    
    /**
     * 结算状态 已失效
     */
    public static final Integer EXPIRE = 3;
    
    /**
     * 返利类型 拉新
     */
    public static final Integer LASHIN = 0;
    /**
     * 返利类型 续费
     */
    public static final Integer RENEW = 1;
    /**
     * 返利类型 差额
     */
    public static final Integer BALANCE = 2;
    
    public static final Integer NO_DATA = 3;
    
    public static final String LASH_NAME = "拉新";
    
    public static final String RENEW_NAME = "续费";
    
    public static final String BALANCE_NAME = "差额";
    
}
