package com.xiliulou.electricity.vo.activity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author HeYafeng
 * @description 小程序-返现活动-我的战绩-我的收入
 * @date 2024/1/2 16:30:40
 */
@Data
public class InvitationActivityIncomeDetailVO {
    
    /**
     * 已获奖励
     */
    private BigDecimal totalIncome;
    
    /**
     * 首返奖励
     */
    private BigDecimal firstTotalIncome;
    
    /**
     * 首返奖励总人数
     */
    private Integer firstTotalMemCount;
    
    /**
     * 续返奖励
     */
    private BigDecimal renewTotalIncome;
    
    /**
     * 续返奖励总人数
     */
    private Integer renewTotalMemCount;
    
    /**
     * 邀请明细
     */
    private List<InvitationActivityDetailVO> detailVOList;
    
}
