package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 是否限制提前续费，0:不限制,1:限制
     */
    private Integer advanceRenewal;
    
    /**
     * 提前续费天数
     */
    private Integer advanceRenewalDay;
    
    /**
     * 优惠券id
     */
    private Integer couponId;
    
    /**
     * 套餐绑定的所有优惠券id
     */
    private String couponIds;
    
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
     * 套餐过期服务费
     */
    private BigDecimal serviceCharge;
    
    /**
     * 套餐冻结服务费
     */
    private BigDecimal freezeServiceCharge;
    
    /**
     * 分期套餐服务费
     */
    private BigDecimal installmentServiceFee;
    
    /**
     * 分期套餐首期费用
     */
    private BigDecimal downPayment;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 套餐业务类型：0，换电套餐；1，车电一体套餐, 2. 企业渠道换电套餐
     *
     * @see BatteryMemberCardBusinessTypeEnum
     */
    private Integer businessType;
    
    private Integer delFlag;
    
    private Integer tenantId;
    
    private Long createTime;
    
    private Long updateTime;
    
    /**
     * 排序参数，前台展示给骑手时排序使用
     */
    private Long sortParam;
    
    /**
     * 分组类型，0-系统分组，1-用户分组。
     */
    private Integer groupType;
    
    /**
     * 套餐绑定的所有用户分组id
     */
    private String userInfoGroupIds;
    
    public static final Integer GROUP_TYPE_SYSTEM = 0;
    
    public static final Integer GROUP_TYPE_USER = 1;
    
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
    
    public static final Integer BUSINESS_TYPE_ENTERPRISE = 2;
    
    /**
     * 业务类型-分期换电套餐
     */
    public static final Integer BUSINESS_TYPE_INSTALLMENT_BATTERY = 4;

    //租赁类型 0:不限 1:新租 2:续租
    public static final Integer RENT_TYPE_UNLIMIT = 0;
    
    public static final Integer RENT_TYPE_NEW = 1;
    
    public static final Integer RENT_TYPE_OLD = 2;
    
    /**
     * 赠送优惠券
     */
    public static final Integer SEND_COUPON_YES = 0;
    
    /**
     * 不赠送优惠券
     */
    public static final Integer SEND_COUPON_NO = 1;
    
    /**
     * 是否免押 0--是 1--否
     */
    public static final Integer FREE_DEPOSIT = 0;
    
    public static final Integer UN_FREE_DEPOSIT = 1;
    
    /**
     * 是否限制提前续费，0:不限制,1:限制
     */
    public static final Integer ADVANCE_RENEWAL_UNLIMIT = 0;
    
    public static final Integer ADVANCE_RENEWAL_LIMIT = 1;
}
