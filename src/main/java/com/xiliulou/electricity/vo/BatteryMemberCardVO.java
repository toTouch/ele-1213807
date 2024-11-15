package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.MemberCardBatteryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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
    
    private String franchiseeName;
    
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
    
    private List<String> batteryModels;
    
    private String couponName;
    
    /**
     * 优惠券减免金额
     */
    private BigDecimal amount;
    
    private Boolean editUserMembercard = Boolean.TRUE;
    
    /**
     * 排序参数，前台展示给骑手时排序使用
     */
    private Long sortParam;
    
    /**
     * 绑定的所有电池型号
     */
    private List<MemberCardBatteryType> batteryType;
    
    /**
     * 套餐绑定的优惠券信息
     */
    private List<CouponSearchVo> coupons;
    
    /**
     * 套餐绑定的用户分组信息
     */
    private List<SearchVo> userInfoGroups;
    
    /**
     * 分组类型，0-系统分组，1-用户分组。
     */
    private Integer groupType;
    
    /**
     * 租户名称
     */
    private String tenantName;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 分期套餐服务费
     */
    private BigDecimal installmentServiceFee;
    
    /**
     * 分期套餐首期费用
     */
    private BigDecimal downPayment;
    
    /**
     * 分期数
     */
    private Integer installmentNo;
    
    /**
     * 分期套餐剩余每期费用
     */
    private BigDecimal remainingCost;
}
