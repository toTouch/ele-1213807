package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zgw
 * @date 2023/3/23 10:33
 * @mood
 */
@Data
public class ChannelActivityHistoryVo {
    
    private Long id;
    
    private Long uid;
    
    private String name;
    
    private String phone;
    
    private Long inviteUid;
    
    private String inviteName;
    
    private String invitePhone;
    
    private Long channelUid;
    
    private String channelName;
    
    private String channelPhone;
    
    /**
     * 邀请状态 1--已参与 2--邀请成功 3--已过期 4--被替换
     */
    private Integer status;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Integer tenantId;
}
