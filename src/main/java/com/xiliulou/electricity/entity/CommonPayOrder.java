package com.xiliulou.electricity.entity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: XILIULOU
 * @description:
 * @author: LXC
 * @create: 2021-02-23 09:45
 **/
@Data
@Builder
public class CommonPayOrder {
    //支付金额
    private BigDecimal payAmount;
   //
    private Long uid;
    //
    private String orderId;
    //订单类型
    private Integer orderType;
    //额外参数
    private String attach;
    //
    private Integer tenantId;
    /**
     * 商品描述
     */
    private String description;


}
