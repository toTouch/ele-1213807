package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * (BatteryMemberCard)实体类
 *
 * @author zzlong
 * @since 2023-07-07 14:06:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_battery_member_card")
public class BatteryMemberCard {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 套餐名称
     */
    private String name;
    /**
     * 押金
     */
    private BigDecimal deposit;
    /**
     * 租金
     */
    private BigDecimal rentPrice;
    /**
     * 租金单价
     */
    private BigDecimal rentPriceUnit;
    /**
     * 租期
     */
    private Integer validDays;
    /**
     * 租期单位 0：分钟，1：天
     */
    private Integer rentUnit;

    private Long franchiseeId;
    /**
     * 租赁类型
     */
    private Integer rentType;
    /**
     * 是否赠送优惠券 0:赠送,1:不赠送
     */
    private Integer sendCoupon;
    /**
     * 上架状态 0:上架,1:下架
     */
    private Integer status;
    /**
     * 0:不限制,1:限制
     */
    private Integer limitCount;
    /**
     * 使用次数
     */
    private Long useCount;
    /**
     * 优惠券id
     */
    private Integer couponId;
    /**
     * 是否退租金 0--是 1--否
     */
    private Integer isRefund;
    /**
     * 退租金天数限制
     */
    private Integer refundLimit;
    /**
     * 是否免押 0--是 1--否
     */
    private Integer freeDeposite;
    /**
     * 服务费
     */
    private BigDecimal serviceCharge;
    /**
     *
     */
//    private Integer type;
    /**
     * 备注
     */
    private String remark;
    /**
     * 套餐业务类型：0，换电套餐；1，车电一体套餐
     */
    private Integer businessType;

    private Integer delFlag;

    private Integer tenantId;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //上架状态 0:上架,1:下架
    public static final Integer STATUS_UP = 0;
    public static final Integer STATUS_DOWN = 1;

    //租期单位 0：分钟，1：天
    public static final Integer RENT_UNIT_MINUTES = 0;
    public static final Integer RENT_UNIT_DAY = 1;

    //0:不限制,1:限制
    public static final Integer UN_LIMIT = 0;
    public static final Integer LIMIT = 1;

    //是否 0--是 1--否
    public static final Integer YES = 0;
    public static final Integer NO = 1;

    //是否显示(0:显示,1:隐藏)
    public static final Integer DISPLAY = 0;
    public static final Integer HIDDEN = 1;

    //套餐业务类型：0，换电套餐；1，车电一体套餐
    public static final Integer BUSINESS_TYPE_BATTERY = 0;
    public static final Integer BUSINESS_TYPE_BATTERY_CAR = 1;

    //租赁类型 0:不限 1:新租 2:续租 3:不限+续租
    public static final Integer RENT_TYPE_OLD = 2;
    public static final Integer RENT_TYPE_NEW = 1;
    public static final Integer RENT_TYPE_UNLIMIT = 0;
    public static final Integer RENT_TYPE_UNLIMIT_OLD = 3;


}
