package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.SpecificPackagesEnum;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 优惠券规则表(TCoupon)实体类
 *
 * @author makejava
 * @since 2021-04-14 09:28:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_coupon")
public class Coupon {

    @NotNull(message = "优惠券id不能为空!", groups = {UpdateGroup.class})
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
    * 优惠券名称
    */
    @NotEmpty(message = "优惠券名称不能为空!", groups = {CreateGroup.class})
    private String name;
    /**
     * 适用类型  1--邀请活动优惠券  2--普通活动优惠券
     */
    private Integer applyType;
    /**
     * 类型  1--自营  2--加盟商
     */
    private Integer type;
    /**
    * 优惠券状态，分为 1--上架，2--下架
    */
    private Integer status;
    /**
    * 优惠类型，1--减免券，2--打折券，3-天数劵
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
     * 加盟商Id
     */
    private Integer franchiseeId;

    /**
     * 租户
     */
    private Integer tenantId;

    /**
     * 是否可叠加 0：否，1：是
     */
    @NotNull(message = "优惠券叠加使用方式不能为空!", groups = {CreateGroup.class})
    private Integer superposition;

    /**
     * 是否指定套餐使用 1-指定套餐, 2-不指定套餐,适用于所有套餐
     * @see SpecificPackagesEnum
     */
    private Integer specificPackages;
    
    /**
     * 套餐有效期,通过用户发放时间 + 有效天数计算出截止时间,实际字段在 UserCoupon 中
     * @see UserCoupon#deadline
     */
    @TableField(exist = false)
    private Long deadline;
    
    /**
     * -1 -- 不限制,0 -- 租车，1 -- 租电，2 -- 车电一体
     */
    private Integer useScope;

    public static final Integer SUPERPOSITION_NO = 0;
    
    public static final Integer SUPERPOSITION_YES = 1;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;


    //下架
    public static final Integer STATUS_OFF = 2;
    //上架
    public static final Integer STATUS_ON = 1;


    //减免劵
    public static final Integer FULL_REDUCTION=1;

    //打折劵
    public static final Integer DISCOUNT=2;

    //天数券
    public static final Integer DAY_VOUCHER=3;

    //平台劵
    public static final Integer TYPE_SYSTEM = 1;
    //加盟商劵
    public static final Integer TYPE_FRANCHISEE = 2;


    //邀请活动优惠券
    public static final Integer APPLY_TYPE_SHARE = 1;
    //普通活动优惠券
    public static final Integer APPLY_TYPE_DEFAULT = 2;

}
