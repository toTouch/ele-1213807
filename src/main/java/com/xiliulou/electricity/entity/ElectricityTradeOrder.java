package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 10:17
 **/
@Data
public class ElectricityTradeOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    //具体订单 订单号
    private String orderNo;
    //订单类型  (1:月卡,2...)
    private Integer orderType;
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

    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = -1;

    public static final Integer ORDER_TYPE_MEMBER_CARD = 1;


}
