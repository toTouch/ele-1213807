package com.xiliulou.electricity.vo;


import lombok.Data;

/**
 * @author : renhang
 * @description CouponPackageOperateRecordPageVO
 * @date : 2025-02-05 10:20
 **/
@Data
public class CouponPackageOperateRecordPageVO {

    /**
     * package_id
     */
    private Long packageId;

    /**
     * 用户Id
     */
    private Long uid;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户phone
     */
    private String phone;

    /**
     * 优惠劵发放人姓名
     */
    private String operateName;

    private Integer tenantId;

    private Long createTime;

}
