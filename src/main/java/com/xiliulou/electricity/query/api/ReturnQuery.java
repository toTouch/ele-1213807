package com.xiliulou.electricity.query.api;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : eclair
 * @date : 2021/11/9 9:49 上午
 */
@Data
public class ReturnQuery {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 电池类型
     */
    private String type;
    /**
     * 产品的key
     */
    private String productKey;
    /**
     * 产品的deviceName
     */
    private String deviceName;
    /**
     * 归还的电池编号
     */
    private String returnBatteryName;


}
