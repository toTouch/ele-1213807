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
     * 如果不是同一个柜机，重新扫码换电不拦截，1
     */
    private Integer isReScanExchange;
    
    //微信公众号来源
    public static final Integer SOURCE_WX_MP = 1;
    
    //微信小程序来源
    public static final Integer SOURCE_WX_RPO = 2;
    
    
    public static final Integer NORMAL_EXCHANGE = 1;
    
    public static final Integer RESCAN_EXCHANGE = 1;
    
}
