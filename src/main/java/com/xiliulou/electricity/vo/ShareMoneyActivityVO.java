package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 活动表(Activity)实体类
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@Data
public class ShareMoneyActivityVO {

    private Integer id;
    /**
     * 活动名称
     */
    private String name;
    /**
     * 活动状态，分为 1--上架，2--下架
     */
    private Integer status;
    /**
     * 活动类型，分为 1--自营，2--代理
     */
    private Integer type;
    /**
     * 活动说明
     */
    private String description;
    /**
     * 创建人uid
     */
    private Long uid;
    /**
     * 创建人用户名
     */
    private String userName;
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
     * 加盟商Id
     */
    private Integer franchiseeId;

    /**
     * 租户
     */
    private Integer tenantId;

    /**
     * 小时
     */
    private Integer hours;

    /**
     * 金额
     */
    private BigDecimal money;

    //邀请人数
    private Integer count;

    /**
     * 总金额
     */
    private BigDecimal totalMoney;






}
