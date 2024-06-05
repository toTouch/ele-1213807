package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * 渠道活动邀请人
 */
@Data
public class FinalJoinChannelActivityHistoryVO {
    
    private Long id;
    
    private Long uid;
    
    private String userName;
    
    private Long joinUid;
    
    private String joinUserName;
}
