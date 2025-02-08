package com.xiliulou.electricity.entity;


import com.xiliulou.electricity.enums.UserInfoActivitySourceEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (UserInfoExtra)实体类
 *
 * @author Eclair
 * @since 2024-02-18 10:39:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_info_extra")
public class UserInfoExtra {
    
    private Long uid;
    
    /**
     * 商户ID
     */
    private Long merchantId;
    
    /**
     * 渠道员uid
     */
    private Long channelEmployeeUid;
    
    /**
     * 场地员工uid
     */
    private Long placeUid;
    
    private Long placeId;
    
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
    
    private Integer tenantId;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 参与成功的活动来源：0-无参与成功的活动，1-邀请返券,2-邀请返现,3-套餐返现,4-渠道邀请,5-商户邀请
     * @see UserInfoActivitySourceEnum
     */
    private Integer activitySource;
    
    /**
     * 邀请人uid(商户活动的邀请人来源于user表，其它活动的邀请人来源于userInfo表)
     */
    private Long inviterUid;
    
    /**
     * 最新参与的活动的类型：0-无参与的活动，1-邀请返券,2-邀请返现,3-套餐返现,4-渠道邀请,5-商户邀请
     * @see UserInfoActivitySourceEnum
     */
    private Integer latestActivitySource;
    
    /**
     * 是否对换电套餐购买次数限制:0-不限制 1-限制
     *
     */
    private Integer eleLimit;

    /**
     * 每月最小时间戳，用于判断次数统计是否为本月
     */
    private Long minTimeOfMonth;

    /**
     * 用户每月套餐冻结次数
     */
    private Integer packageFreezeCount;

    /**
     * '是否为流失用户 0 - 是, 1 - 否
     */
    private Integer lostUserStatus;

    /**
     * 流失时间
     */
    private Long lostUserTime;

    /**
     * 流失用户首次拉新返利时间
     */
    private Long lostUserFirstRebateTime;
}
