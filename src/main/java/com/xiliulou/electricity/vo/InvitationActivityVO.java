package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.BatteryMemberCard;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-06-16:11
 */
@Data
public class InvitationActivityVO {
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
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;

    private List<BatteryMemberCard> memberCardList;
}
