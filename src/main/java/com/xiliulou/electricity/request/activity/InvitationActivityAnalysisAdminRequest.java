package com.xiliulou.electricity.request.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * @author HeYafeng
 * @description saas-邀请分析/已获奖励
 * @date 2024/1/4 13:46:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationActivityAnalysisAdminRequest {
    
    /**
     * 1-昨日（昨天0:00-23:59） 2-本月（当月一号0:00-当前时间，默认值） 3-累计（用户首次邀请新人的时间-当前时间）
     */
    @Range(min = 1, max = 3)
    @NotNull
    private Integer timeType;
    
    private Long beginTime;
    
    private Long endTime;
}
