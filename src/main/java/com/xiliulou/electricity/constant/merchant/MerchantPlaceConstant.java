package com.xiliulou.electricity.constant.merchant;

/**
 * @author maxiaodong
 * @date 2024/2/19 11:43
 * @desc 场地常量
 */
public class MerchantPlaceConstant {
    /**
     * 状态：启用
     */
    public final static Integer ENABLE = 0;
    /**
     * 状态：禁用
     */
    public final static Integer DISABLE = 1;
    
    /**
     * 类型:0-绑定
     */
    public final static Integer BIND = 0;
    /**
     * 类型1-解绑
     */
    public final static Integer UN_BIND = 1;
    
    /**
     * 商户结算标记 0-是
     */
    public final static Integer MONTH_SETTLEMENT_YES = 0;
    /**
     * 商户结算标记 1-否
     */
    public final static Integer MONTH_SETTLEMENT_NO = 1;
    
    /**
     * 商户结算标记（电费） 0-是
     */
    public final static Integer MONTH_SETTLEMENT_POWER_YES = 0;
    /**
     * 商户结算标记（电费） 1-否
     */
    public final static Integer MONTH_SETTLEMENT_POWER_NO = 1;
    
    /**
     * 商户场地结算标记（电费） 1-是
     */
    public final static Integer PLACE_MONTH_SETTLEMENT_POWER_YES = 1;
    /**
     * 商户场地结算标记（电费） 0-否
     */
    public final static Integer PLACE_MONTH_SETTLEMENT_POWER_NO = 0;
    
    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
    
    /**
     * 柜机数量限制
     */
    public final static Integer BIND_CABINET_COUNT_LIMIT = 20;
}
