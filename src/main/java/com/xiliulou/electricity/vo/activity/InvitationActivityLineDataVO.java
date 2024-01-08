package com.xiliulou.electricity.vo.activity;

import com.xiliulou.electricity.utils.DateUtils;
import lombok.Data;

/**
 * @author HeYafeng
 * @description 小程序-返现活动-我的战绩-累计成功邀请详情-折线图
 * @date 2024/1/3 14:10:33
 */
@Data
public class InvitationActivityLineDataVO {
    
    /**
     * 邀请时间
     */
    private Long createTime;
    
    /**
     * 邀请总人数
     */
    private Integer totalShareCount;
    
    /**
     * 邀请成功总人数
     */
    private Integer totalInvitationCount;
}
