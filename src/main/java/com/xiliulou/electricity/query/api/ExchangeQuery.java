package com.xiliulou.electricity.query.api;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2021/11/10 1:51 下午
 */
@Data
public class ExchangeQuery {
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 电池类型
     */
    private String type;
    /**
     * 放入的电池名字
     */
    private String returnBatteryName;
    /**
     * 柜机的product
     */
    private String productKey;
    /**
     * 柜机的deviceName
     */
    private String deviceName;
}
