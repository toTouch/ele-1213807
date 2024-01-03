package com.xiliulou.electricity.vo.activity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 小程序-返现活动-我的战绩
 * @date 2024/1/2 16:30:40
 */
@Builder
@Data
public class InvitationActivityStaticsVO {
    
    /**
     * 累计成功邀请人数
     */
    private Integer totalInvitationCount;
    
    /**
     * 我的收入
     */
    private BigDecimal totalMoney;
}
