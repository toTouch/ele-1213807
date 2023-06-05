package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * (InvitationActivity)实体类
 *
 * @author Eclair
 * @since 2023-06-01 15:55:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_invitation_activity")
public class InvitationActivity {

    private Long id;
    /**
     * 活动名称
     */
    private String name;
    /**
     * 类型  1--自营  2--加盟商
     */
    private Integer type;
    /**
     * 活动状态，分为 1--上架，2--下架
     */
    private Integer status;
    /**
     * 描述
     */
    private String description;
    /**
     * 有效时间
     */
    private Integer hours;
    /**
     * 奖励类型  1--固定金额  2--套餐比例
     */
    private Integer discountType;

    /**
     * 首次购买返现
     */
    private BigDecimal firstReward;
    /**
     * 非首次购买返现
     */
    private BigDecimal otherReward;
    /**
     * 创建用户Id
     */
    private Long operateUid;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    /**
     * 租户id
     */
    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //活动状态，分为 1--上架，2--下架
    public static final Integer STATUS_UP = 1;
    public static final Integer STATUS_DOWN = 2;

    public static final Integer TYPE_DEFAULT = 0;

}
