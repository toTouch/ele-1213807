package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author : renhang
 * @description CouponPackagePageVO
 * @date : 2025-01-17 11:24
 **/
@Data
public class CouponPackagePageVO {

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
     * 包含的优惠券名称
     */
    private String couponNameStr;

    /**
     * 包含优惠券作用
     */
    private String effectStr;

    /**
     * 是否可购买 0:否,1:是
     */
    private Integer isCanBuy;


    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 加盟商ID
     */
    private Long franchiseeId;

    /**
     * 加盟商名称
     */
    private String franchiseeName;


    /**
     * 创建用户名
     */
    private String userName;

    /**
     * 创建时间
     */
    private Long createTime;


}
