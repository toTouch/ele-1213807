package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券表(TCoupon)实体类
 *
 * @author makejava
 * @since 2021-04-14 09:27:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_coupon")
public class UserCoupon {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 优惠券来源 0--后台发放，1--邀请好友活动
     */
    private Integer source;
    /**
     * 活动id
     */
    private Integer activityId;
    /**
     * 活动规则id
     */
    private Long activityRuleId;
    /**
     * 优惠券id
     */
    private Integer couponId;
    /**
     * 优惠券名称，也就是t_coupon的名称
     */
    private String name;
    /**
     * 优惠类型，1--减免券，2--打折券，3-体验劵
     */
    private Integer discountType;
    /**
     * 用户uid
     */
    private Long uid;
    /**
     * 用户手机号
     */
    private String phone;
    /**
     * 优惠券截止time
     */
    private Long deadline;
    /**
     * 优惠券使用的订单id
     */
    private String orderId;
    /**
     * 优惠券来源的订单id
     */
    private String sourceOrderId;
    /**
     * 加盟商id
     */
    private Integer franchiseeId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    /**
     * 优惠券状态1--未使用， 2--已使用 ，3--已过期
     *         4--已核销， 5--使用中， 6--已失效
     */
    private Integer status;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    /**
     * 租户
     */
    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //后台发送
    public static final Integer TYPE_SOURCE_ADMIN_SEND = 1;

    //邀请好友活动领取
    public static final Integer TYPE_SOURCE_SHARE_ACTIVITY = 2;

    public static final Integer TYPE_SOURCE_BUY_PACKAGE = 3;

    //未使用
    public static final Integer STATUS_UNUSED = 1;
    //已使用
    public static final Integer STATUS_USED = 2;
    //已过期
    public static final Integer STATUS_EXPIRED = 3;
    //已核销
    public static final Integer STATUS_DESTRUCTION = 4;
    // 使用中
    public static final Integer STATUS_IS_BEING_VERIFICATION = 5;
    //已失效
    public static final Integer STATUS_IS_INVALID = 6;

    //减免劵
    public static final Integer FULL_REDUCTION = 1;

    //打折劵
    public static final Integer DISCOUNT = 2;

    //体验劵
    public static final Integer DAYS = 3;

}
