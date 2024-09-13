package com.xiliulou.electricity.mq.model;

import lombok.Data;

/**
 * @author maxiaodong
 * @date 2024/8/27 10:36
 * @desc
 */
@Data
public class ProfitSharingTradeOrderUpdate {
    /**
     * 业务支付订单号
     */
    private String orderNo;
    
    /**
     * 第三方支付订单号
     */
    private String thirdOrderNo;
    
    /**
     * 订单类型：1-电池滞纳金，2-保险支付，3-换电套餐
     */
    private Integer orderType;
    
    /**
     * 交易状态 1：成功
     */
    private Integer tradeStatus;
}
