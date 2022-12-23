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

    private String jsonSingleFee;
    //额外参数
    private String attach;
    //
    private Integer tenantId;
    /**
     * 商品描述
     */
    private String description;

    //押金支付
    public static final Integer ORDER_TYPE_DEPOSIT = 1;

    //保险支付
    public static final Integer ORDER_TYPE_INSURANCE = 2;

    //换电套餐
    public static final Integer ORDER_TYPE_MEMBER_CARD = 3;

    //租车押金
    public static final Integer ORDER_TYPE_RENT_CAR_DEPOSIT = 4;

    //租车套餐
    public static final Integer ORDER_TYPE_RENT_CAR_MEMBER_CARD = 5;


    //保险附加信息
    public static final String ATTACH_INSURANCE_UNION_DEPOSIT = "insurance_union_deposit";

}
