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
public class SelectionExchangeCheckQuery {
    
    /**
     * 换电柜id
     */
    @NotNull(message = "换电柜id不能为空!")
    private Integer eid;
    
    /**
     * 多次换电：null或者0 正常换电：1
     */
    private Integer exchangeBatteryType;
    
    /**
     * 如果不是同一个柜机，重新扫码换电不拦截，1
     */
    private Integer isReScanExchange;


    /**
     * 小程序版本号
     */
    private String version;
    
    
    public static final Integer NORMAL_EXCHANGE = 1;
    
    public static final Integer RESCAN_EXCHANGE = 1;
    
}
