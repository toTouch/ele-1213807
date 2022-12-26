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
 * @create: 2020-12-03 09:45
 **/
@Data
@TableName("t_electricity_member_card_order")
public class ElectricityMemberCardOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    //支付金额
    private BigDecimal payAmount;
    //有效天数
    private Integer validDays;
    //最大使用次数
    private Long maxUseCount;
    //
    private Long uid;
    //
    private String orderId;
    // 0,未支付,1,支付成功 2,支付失败
    private Integer status;
    //月卡id
    private Integer memberCardId;
    //用户名
    private String userName;
    //月卡类型
    private Integer memberCardType;
    /**
     * 套餐名称
     */
    private String cardName;
    //创建时间
    private Long createTime;
    //修改时间
    private Long updateTime;

    //租户id
    private Integer tenantId;

    private Long franchiseeId;

    private Integer isBindActivity;

    private Integer payType;

    /**
     * 活动id
     */
    private Integer activityId;

    /**
     * 套餐模式 0--电池套餐 1--租车套餐
     */
    private Integer memberCardModel;

    //优惠券Id
    private Long couponId;

    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = 0;

    public static final Integer BIND_ACTIVITY = 1;
    public static final Integer UNBIND_ACTIVITY = 0;

    public static final Integer BATTERY_MEMBER_CARD = 0;
    public static final Integer RENT_CAR_MEMBER_CARD = 1;

    public static final Integer ONLINE_PAYMENT = 0;
    public static final Integer OFFLINE_PAYMENT = 1;
}
