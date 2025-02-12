package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author : renhang
 * @description 优惠券包
 * @date : 2025-01-16 14:57
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_coupon_package")
public class CouponPackage {

    /**
     * Id
     */
    private Long id;

    /**
     * 卷包名称
     */
    private String name;

    /**
     * 包含的优惠券数量
     */
    private Integer couponCount;

    /**
     * 是否可购买 0:否,1:是
     */
    private Integer isCanBuy;

    /**
     * 创建用户名
     */
    private String userName;

    /**
     * 购买金额
     */
    private BigDecimal amount;


    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 加盟商ID
     */
    private Long franchiseeId;


    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;


    /**
     * 可以购买
     */
    public static final Integer CAN_BUY = 1;


    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
}
