package com.xiliulou.electricity.vo.activity;

import com.xiliulou.electricity.constant.NumberConstant;
import lombok.Data;

/**
 * @author HeYafeng
 * @description 小程序-返现活动-我的战绩-累计成功邀请详情
 * @date 2024/1/2 16:30:40
 */
@Data
public class InvitationActivityAnalysisVO {
    
    /**
     * 邀请总人数(昨日/本月/累计)
     */
    private Integer totalShareCount;
    
    /**
     * 邀请成功总人数(昨日/本月/累计)
     */
    private Integer totalInvitationCount;
    
}
