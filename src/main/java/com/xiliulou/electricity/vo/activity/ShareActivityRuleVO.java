package com.xiliulou.electricity.vo.activity;

import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/8/10 15:00
 * @Description:
 */

@Data
public class ShareActivityRuleVO {
    
    private Long id;
    
    /**
     * 活动id
     */
    private Integer activityId;
    
    /**
     * 触发人数
     */
    private Integer triggerCount;
    
    /**
     * 优惠券id
     */
    private Integer couponId;
    
    /**
     * 优惠券名称
     */
    private String couponName;
    
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
     * 租户
     */
    private Integer tenantId;
    
}
