package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * 套餐返现活动邀请人
 */
@Data
public class FinalJoinInvitationActivityHistoryVO {
    
    private Long id;
    
    private Long uid;
    
    private String userName;
    
    private Long joinUid;
    
    private String joinUserName;
}
