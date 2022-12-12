package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.HRP
 * @create: 2022-11-07 10:17
 **/
@Data
@TableName("t_union_trade_order")
public class UnionTradeOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    //具体订单 订单号
    private String jsonOrderId;
    //订单类型  (1:月卡,2...)
    private String jsonOrderType;
    //每笔订单的单独支付费用
    private String jsonSingleFee;
    //交易订单(系统发起支付时的订单号,我们系统交易订单唯一)
    private String tradeOrderNo;
    //支付费用
    private BigDecimal totalFee;
    //交易状态
    private Integer status;
    private String clientId;
    //  渠道交易单号,渠道商唯一交易单号(支付回调中拿到,退款时候用)
    private String channelOrderNo;
    private Long uid;
    //创建时间
    private Long createTime;
    //修改时间
    private Long updateTime;

    //
    private Integer tenantId;

    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = -1;

    //月卡支付
    public static final Integer ORDER_TYPE_MEMBER_CARD = 1;


    //保险附加信息
    public static final String ATTACH_UNION_INSURANCE_AND_DEPOSIT = "insuranceUnionDeposit";


    public static final String ATTACH_INTEGRATED_PAYMENT = "integratedPayment";


}
