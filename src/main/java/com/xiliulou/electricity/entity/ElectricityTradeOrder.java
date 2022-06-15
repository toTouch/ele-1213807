package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 10:17
 **/
@Data
@TableName("t_electricity_trade_order")
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

    //
    private Integer tenantId;

    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = -1;

    //月卡支付
    public static final Integer ORDER_TYPE_MEMBER_CARD = 1;
    //押金支付
    public static final Integer ORDER_TYPE_DEPOSIT = 2;
    //电池服务费支付
    public static final Integer ORDER_TYPE_BATTERY_SERVICE_FEE = 3;
    //租车押金
    public static final Integer ORDER_TYPE_RENT_CAR_DEPOSIT = 4;

    //租车月卡支付
    public static final Integer ORDER_TYPE_RENT_MEMBER_CARD = 5;

    //押金支付附加信息
    public static final String ATTACH_DEPOSIT = "deposit";

    //押金支付附加信息
    public static final String ATTACH_MEMBER = "memberCard";

    //电池服务费附加信息
    public static final String ATTACH_BATTERY_SERVICE_FEE = "batteryServiceFee";

    //租车押金附加信息
    public static final String ATTACH_RENT_CAR_DEPOSIT = "rentCarDeposit";

    //租车月卡附加信息
    public static final String ATTACH_RENT_CAR_MEMBER_CARD = "rentCarMemberCard";


}
