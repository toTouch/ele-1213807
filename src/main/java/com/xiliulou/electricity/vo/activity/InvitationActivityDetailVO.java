package com.xiliulou.electricity.vo.activity;

import lombok.Builder;
import lombok.Data;

/**
 * @author HeYafeng
 * @description 小程序-返现活动-我的战绩-累计成功邀请详情-邀请明细
 * @date 2024/1/3 14:10:33
 */
@Builder
@Data
public class InvitationActivityDetailVO {
    
    /**
     * 参与人uid
     */
    private Long joinUid;
    
    /**
     * 参与人姓名
     */
    private String joinName;
    
    /**
     * 参与人手机号
     */
    private String joinPhone;
    
    /**
     * 参与时间
     */
    private Long joinTime;
    
    /**
     * 活动id
     */
    private Long activityId;
    
    /**
     * 活动名称
     */
    private String activityName;
}
