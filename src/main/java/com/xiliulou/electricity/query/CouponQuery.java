package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author: lxc
 * @Date: 2021/4/15 16:02
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponQuery {
    private Long size;
    private Long offset;

    @NotNull(message = "优惠券id不能为空!", groups = {UpdateGroup.class})
    private Integer id;

    /**
     * 优惠券名称
     */
    @NotEmpty(message = "优惠券名称不能为空!", groups = {CreateGroup.class})
    private String name;
    /**
     * 优惠类型，1--减免券，2--打折券，3-体验劵
     */
    @NotNull(message = "优惠类型不能为空!", groups = {CreateGroup.class})
    private Integer discountType;

    /**
     * 优惠金额
     */
    private BigDecimal amount;

    /**
     * 折扣
     */
    private BigDecimal discount;
    /**
     * 天数劵
     */
    private Integer count;

    /**
     * 有效天数
     */
    @NotNull(message = "有效天数不能为空!", groups = {CreateGroup.class})
    private Integer days;
    /**
     * 优惠券描述
     */
    @NotEmpty(message = "优惠券描述不能为空!", groups = {CreateGroup.class})
    private String description;

    /**
     * 优惠券状态，分为 1--上架，2--下架
     */
    private Integer status;
    /**
     * 加盟商Id
     */
    private Long franchiseeId;
    
    private List<Long> franchiseeIds;
    /**
     * 适用类型  1--邀请活动优惠券  2--普通活动优惠券
     */
    private Integer applyType;

    /**
     * 类型  1--自营  2--加盟商
     */
    private Integer type;


    private Integer tenantId;

    /**
     * 优惠券ID集
     */
    private List<Long> ids;

    /**
     * 是否可叠加 0：否，1：是
     */
    @NotNull(message = "优惠券叠加使用方式不能为空!", groups = {CreateGroup.class})
    private Integer superposition;

    /**
     * 换电套餐IDs
     */
    private List<Long> batteryPackages;

    /**
     * 租车套餐IDs
     */
    private List<Long> carRentalPackages;

    /**
     * 车电一体套餐IDs
     */
    private List<Long> carWithBatteryPackages;

}
