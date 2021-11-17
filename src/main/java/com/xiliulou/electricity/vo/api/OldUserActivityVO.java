package com.xiliulou.electricity.vo.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 活动表(NewUserActivity)实体类
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OldUserActivityVO {

    private Integer id;
    /**
    * 活动名称
    */
    private String name;
    /**
    * 活动状态，分为 1--上架，2--下架
    */
    private Integer status;
    /**
     * 活动类型，分为 1--自营，2--代理
     */
    private Integer type;
    /**
     * 时间类型，分为 1--有限制，2--无限制
     */
    private Integer timeType;
    /**
     * 活动开始时间
     */
    private Integer beginTime;
    /**
     * 活动结束时间
     */
    private Integer endTime;
    /**
     * 奖励类型，1--次数，2--优惠券
     */
    private Integer discountType;

    /**
     * 换电次数
     */
    private Integer count;


    /**
     * 优惠券id
     */
    private Integer couponId;

    /**
    * 活动说明
    */
    private String description;
    /**
    * 0--正常 1--删除
    */
    private Integer delFlag;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 修改时间
    */
    private Long updateTime;


    /**
     * 加盟商Id
     */
    private Integer franchiseeId;

    /**
     * 租户
     */
    private Integer tenantId;

    /**
     * 创建人uid
     */
    private Long uid;
    /**
     * 创建人用户名
     */
    private String userName;


    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券状态，分为 1--上架，2--下架
     */
    private Integer couponStatus;
    /**
     * 优惠金额
     */
    private BigDecimal amount;
    /**
     * 有效天数
     */
    private Integer couponDays;
    /**
     * 优惠券描述
     */
    private String couponDescription;

}
