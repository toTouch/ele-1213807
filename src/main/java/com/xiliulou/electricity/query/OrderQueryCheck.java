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
public class OrderQueryCheck {
    
    /**
     * 换电柜id
     */
    @NotNull(message = "换电柜id不能为空!")
    private Integer eid;

    /**
     * 1不是同一个柜机，走正常换电
     */
    private Integer isReScanExchange;
    
    
    /**
     * 非同一个柜机继续换电，为1
     */
    public static final Integer RESCAN_EXCHANGE = 1;
}
