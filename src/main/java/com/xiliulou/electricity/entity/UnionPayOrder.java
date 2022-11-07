package com.xiliulou.electricity.entity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: XILIULOU
 * @description:
 * @author: LXC
 * @create: 2022-11-07 09:45
 **/
@Data
@Builder
public class UnionPayOrder {
    //支付金额
    private BigDecimal payAmount;
   //
    private Long uid;
    //
    private String jsonOrderId;
    //订单类型
    private String jsonOrderType;
    //额外参数
    private String attach;
    //
    private Integer tenantId;
    /**
     * 商品描述
     */
    private String description;


}
