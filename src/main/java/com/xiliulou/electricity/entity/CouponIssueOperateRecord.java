package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    /**
     * 手机号
     */
    private String phone;

    private Long createTime;

    private Long updateTime;

    private Integer tenantId;
}
