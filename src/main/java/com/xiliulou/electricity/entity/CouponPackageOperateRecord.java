package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @Description: t_coupon_package_operate_record
 * @Author: renhang
 * @Date: 2025/02/05
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_coupon_package_operate_record")
public class CouponPackageOperateRecord {

    private Long id;

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
     * 优惠劵发放人Id
     */
    private Long issuedUid;

    /**
     * 优惠劵发放人姓名
     */
    private String operateName;

    private Integer tenantId;


    private Long createTime;

    private Long updateTime;


}
