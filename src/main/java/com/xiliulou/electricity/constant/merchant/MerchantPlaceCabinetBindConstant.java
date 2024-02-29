package com.xiliulou.electricity.constant.merchant;

/**
 * @ClassName : MerchantPlaceCabinetBind
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-19
 */
public class MerchantPlaceCabinetBindConstant {
    
    /**
     * 状态：绑定
     */
    public static final Integer STATUS_BIND = 0;
    
    /**
     * 状态：解绑
     */
    public static final Integer STATUS_UNBIND = 1;
    
    public static final Integer PLACE_MONTH_NOT_SETTLEMENT = 0;
    
    /**
     * saas端电费月结算标记 1-已结算
     */
    public static final Integer POWER_SAAS_MONTH_SETTLEMENT_YES = 1;
    
    /**
     * saas端电费月结算标记 0-未结算
     */
    public static final Integer POWER_SAAS_MONTH_SETTLEMENT_NO = 0;
    
    /**
     * 小程序端电费月结算标记 1-已结算
     */
    public static final Integer POWER_PRO_MONTH_SETTLEMENT_YES = 1;
    
    /**
     * 小程序端电费月结算标记 0-未结算
     */
    public static final Integer POWER_PRO_MONTH_SETTLEMENT_NO = 0;
    
    /**
     * 场地柜机绑定记录：场地费结算类型
     */
    public static final Integer PLACE_CABINET_SETTLE_TYPE_PLACE = 1;
    
    /**
     * 场地柜机记录：saas端电费结算类型
     */
    public static final Integer PLACE_CABINET_SETTLE_TYPE_POWER_SAAS = 2;
    
    /**
     * 场地柜机绑定记录：小程序端电费结算类型
     */
    public static final Integer PLACE_CABINET_SETTLE_TYPE_POWER_PRO = 3;
    
    /**
     * 商户场地绑定记录：场地费结算类型
     */
    public static final Integer MERCHANT_PLACE_SETTLE_TYPE_PLACE = 1;
    
    /**
     * 商户场地绑定记录：小程序端结算类型
     */
    public static final Integer MERCHANT_PLACE_SETTLE_TYPE_POWER_PRO = 2;
}
