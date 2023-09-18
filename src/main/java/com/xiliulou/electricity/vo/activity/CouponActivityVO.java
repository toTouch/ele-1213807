package com.xiliulou.electricity.vo.activity;

import com.xiliulou.electricity.enums.SpecificPackagesEnum;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/29 18:17
 * @Description:
 */

@Data
public class CouponActivityVO {

    private Integer id;

    /**
     * 优惠券名称
     */
    private String name;
    /**
     * 优惠类型，1--减免券，2--打折券，3-体验劵
     */
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
    private String validDays;
    /**
     * 优惠券描述
     */
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
    private Integer superposition;

    /**
     * 是否指定套餐使用 1-指定套餐, 2-不指定套餐,适用于所有套餐
     * @see SpecificPackagesEnum
     */
    private Integer specificPackages;

    /**
     * 创建人uid
     */
    private Long uid;
    /**
     * 创建人用户名
     */
    private String userName;

    private Long createTime;

    private Long updateTime;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    /**
     * 换电套餐列表
     */
    private List<BatteryMemberCardVO> batteryPackages;

    /**
     * 租车套餐列表
     */
    private List<BatteryMemberCardVO> carRentalPackages;

    /**
     * 车电一体套餐列表
     */
    private List<BatteryMemberCardVO> carWithBatteryPackages;

}
