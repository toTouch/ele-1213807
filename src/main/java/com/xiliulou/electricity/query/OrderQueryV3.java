package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
public class OrderQueryV3 {
    
    /**
     * 换电柜id
     */
    @NotNull(message = "换电柜id不能为空!")
    private Integer eid;
    
    /**
     * 下单来源 1--微信公众号 2--小程序
     */
    private Integer source;
    
    /**
     * 多次换电：null或者0 正常换电：1
     */
    private Integer exchangeBatteryType;
    
    /**
     * 如果不是同一个柜机，重新扫码换电不拦截(1:是一个柜机)
     */
    private Integer isReScanExchange;
    
    /**
     * 旧电池检测失败，灵活续费发生套餐转换，灵活续费为换电时，会拦截不分配电池，传1-开始换电
     */
    private Integer secondFlexibleRenewal;
    
    /**
     * 灵活续费操作类型，用于控制分配满电仓的逻辑
     */
    private Integer flexibleRenewalType;

    /**
     * 兼容租退优化（第一次租电，第二次换电的自主开仓）
     */
    private String version;
    
    //微信公众号来源
    public static final Integer SOURCE_WX_MP = 1;
    
    //微信小程序来源
    public static final Integer SOURCE_WX_RPO = 2;
    
    
    public static final Integer NORMAL_EXCHANGE = 1;
    
    public static final Integer RESCAN_EXCHANGE = 1;
    
    public static final Integer SECOND_FLEXIBLE_RENEWAL = 1;


    /**
     * 二次扫码兼容小程序旧版本
     */
    public static final String TWO_SCAN_EXCHANGE_COMPATIBLE_RENT_SELF_OPEN="3.3.29";
}
