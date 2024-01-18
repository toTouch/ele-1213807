package com.xiliulou.electricity.vo.activity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author HeYafeng
 * @description saas-邀请人记录-参与详情-邀请分析、已获奖励
 * @date 2024/1/2 16:30:40
 */
@Data
public class InvitationActivityAnalysisAdminVO {
    
    /**
     * 邀请总人数(昨日/本月/自定义)
     */
    private Integer totalShareCount;
    
    /**
     * 邀请成功总人数(昨日/本月/累计)
     */
    private Integer totalInvitationCount;
    
    /**
     * 已获奖励
     */
    private BigDecimal totalIncome;
    
    /**
     * 首返奖励
     */
    private BigDecimal firstTotalIncome;
    
    /**
     * 续返奖励
     */
    private BigDecimal renewTotalIncome;
    
}
