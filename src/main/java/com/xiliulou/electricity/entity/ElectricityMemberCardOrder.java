package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 09:45
 **/
@Data
@Builder
@TableName("t_electricity_member_card_order")
@AllArgsConstructor
@NoArgsConstructor
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
    // 0,未支付,1,支付成功 2,支付失败,3取消支付
    private Integer status;
    //月卡id
    private Long memberCardId;
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

    private Long storeId;

    private Long franchiseeId;

    private Integer isBindActivity;

    private Integer payType;
    /**
     * 套餐订单使用状态 1未使用,2：使用中,3：已失效, 4:已退租
     */
    private Integer useStatus;
    /**
     * 退租状态 1未退租,2：审核中,3：退租中, 4:退租成功, 5:退租失败
     */
    private Integer refundStatus;

    /**
     * 活动id
     */
    private Integer activityId;

    /**
     * 套餐模式 0--电池套餐 1--租车套餐
     */
    private Integer memberCardModel;

    //优惠券Id
//    private Long couponId;

    /**
     * 套餐购买次数
     */
    private Integer payCount;

    /**
     * 套餐订单来源，1：扫码，2：线上，3：后台
     */
    private Integer source;

    /**
     * 扫码的柜机
     */
    private Long refId;

    /**
     * 套餐赠送的优惠券id
     */
    private Long sendCouponId;

    // 订单状态 0未支付,1支付成功 2支付失败,3取消支付
    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = 2;
    public static final Integer STATUS_CANCELL = 3;
    
    
    public static final Integer BIND_ACTIVITY = 1;
    public static final Integer UNBIND_ACTIVITY = 0;

    public static final Integer BATTERY_MEMBER_CARD = 0;
    public static final Integer RENT_CAR_MEMBER_CARD = 1;

    public static final Integer ONLINE_PAYMENT = 0;
    public static final Integer OFFLINE_PAYMENT = 1;

    //套餐订单来源，1：扫码，2：线上，3：后台
    public static final Integer SOURCE_SCAN = 1;
    public static final Integer SOURCE_NOT_SCAN = 2;
    public static final Integer SOURCE_ARTIFICIAL = 3;

    //套餐订单使用状态 1未使用,2：使用中,3：已失效, 4:已退租
    public static final Integer USE_STATUS_NOT_USE = 1;
    public static final Integer USE_STATUS_USING = 2;
    public static final Integer USE_STATUS_EXPIRE = 3;
    public static final Integer USE_STATUS_REFUND = 4;

    //退租状态 1未退租,2：审核中,3：退租中, 4:退租成功, 5:退租失败, 6:审核拒绝
    public static final Integer REFUND_STATUS_NON = 1;
    public static final Integer REFUND_STATUS_AUDIT = 2;
    public static final Integer REFUND_STATUS_REFUNDING = 3;
    public static final Integer REFUND_STATUS_SUCCESS = 4;
    public static final Integer REFUND_STATUS_FAIL = 5;
    public static final Integer REFUND_STATUS_REFUSED = 6;

}
