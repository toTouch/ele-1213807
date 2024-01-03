package com.xiliulou.electricity.vo.activity;

import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 小程序-返现活动-我的战绩-累计成功邀请详情
 * @date 2024/1/2 16:30:40
 */
@Data
public class InvitationActivityAnalysisVO {
    
    /**
     * 折现图数据
     */
    private List<InvitationActivityLineDataVO> lineDataVOList;
    
    /**
     * 邀请总人数(昨日/本月/累计)
     */
    private Integer totalShareCount;
    
    /**
     * 邀请成功总人数(昨日/本月/累计)
     */
    private Integer totalInvitationCount;
    
    /**
     * 邀请明细
     */
    private List<InvitationActivityDetailVO> detailVOList;
    
}
