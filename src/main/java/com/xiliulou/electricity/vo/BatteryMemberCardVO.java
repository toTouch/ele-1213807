package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-07-14:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryMemberCardVO {

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
     * 退押是否需要审核 0--是 1--否
     */
    private Integer refundDepositeAudit;
    /**
     * 服务费
     */
    private BigDecimal serviceCharge;
    /**
     * 是否显示(0:显示,1:隐藏)
     */
    private Integer display;
    /**
     *
     */
    private Integer type;
    /**
     * 备注
     */
    private String remark;
    /**
     * 套餐业务类型：0，换电套餐；1，车电一体套餐
     */
    private Integer businessType;

    private Long createTime;

    private Long updateTime;

    private String batteryV;
}
