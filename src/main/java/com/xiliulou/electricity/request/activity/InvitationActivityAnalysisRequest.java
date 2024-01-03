package com.xiliulou.electricity.request.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * @author HeYafeng
 * @description 小程序套餐返现->邀请分析
 * @date 2024/1/3 15:59:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationActivityAnalysisRequest {
    
    @NotNull
    private Long size;
    
    @NotNull
    private Long offset;
    
    /**
     * 1-昨日（昨天0:00-23:59） 2-本月（当月一号0:00-当前时间，默认值） 3-累计（用户首次邀请新人的时间-当前时间）
     */
    @Range(min = 1, max = 3)
    @NotNull
    private Integer timeType;
    
    /**
     * 参与状态 1--已参与，2--邀请成功，3--已过期， 4--被替换， 5--活动已下架
     */
    private Integer status;
}
