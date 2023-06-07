package com.xiliulou.electricity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 优惠券表(TCoupon)实体类
 *
 * @author makejava
 * @since 2021-04-14 09:27:59
 */
@Data
public class UserCouponVO {

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

    /**
     * 优惠金额
     */
    private BigDecimal amount;
    /**
     * 折扣
     */
    private BigDecimal discount;

    /**
     * 用户名
     */
    private String userName;

    private Integer superposition;
}
