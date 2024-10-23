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
 * @date 2023-08-14-20:57
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryMemberCardAndTypeVO {
    
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
     * 备注
     */
    private String remark;
    
    /**
     * 套餐业务类型：0，换电套餐；1，车电一体套餐, 2. 企业渠道换电套餐
     */
    private Integer businessType;
    
    private Integer delFlag;
    
    private Integer tenantId;
    
    private Long createTime;
    
    private Long updateTime;
    
    /**
     * 套餐排序参数
     */
    private Long sortParam;
    
    /**
     * 绑定所有电池型号
     */
    private List<MemberCardBatteryType> batteryType;
    
    /**
     * 套餐绑定的所有优惠券id
     */
    private String couponIds;
    
    /**
     * 套餐绑定的所有用户分组id
     */
    private String userInfoGroupIds;
    
    /**
     * 分组类型，0-系统分组，1-用户分组。
     */
    private Integer groupType;
    
    /**
     * 分期套餐服务费
     */
    private BigDecimal installmentServiceFee;
    
    /**
     * 分期套餐首期费用
     */
    private BigDecimal downPayment;
}
