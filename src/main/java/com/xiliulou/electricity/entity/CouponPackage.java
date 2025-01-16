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
    private Boolean isCanBuy;

    /**
     * 创建用户名
     */
    private String userName;

    /**
     * 购买金额
     */
    private BigDecimal amount;

    /**
     * 0:正常 1:删除
     */
    private Integer delFlag;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 加盟商ID
     */
    private Long franchiseeId;

    /**
     * 门店ID
     */
    private Integer storeId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;
}
