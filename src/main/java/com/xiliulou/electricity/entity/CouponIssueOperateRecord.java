package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券规则表(t_coupon_issue_operate_record)实体类
 *
 * @author makejava
 * @since 2022-08-16 09:28:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_coupon_issue_operate_record")
public class CouponIssueOperateRecord {

    private Integer id;

    /**
     * 优惠券Id
     */
    private Integer couponId;

    /**
     * 用户Id
     */
    private Long uid;

    /**
     * 用户姓名
     */
    private String name;

    private String operateName;
    
    /**
     * <p>
     *    Description: 优惠劵发放人Id
     * </p>
     */
    private Long issuedUid;
    
    /**
     * 手机号
     */
    private String phone;

    private Long createTime;

    private Long updateTime;

    private Integer tenantId;
    
    private Long franchiseeId;
}
