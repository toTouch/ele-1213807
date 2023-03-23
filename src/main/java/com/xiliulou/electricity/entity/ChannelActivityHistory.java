package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (ChannelActivityHistory)实体类
 *
 * @author Eclair
 * @since 2023-03-23 09:24:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_channel_activity_history")
public class ChannelActivityHistory {
    
    private Long id;
    
    private Long uid;
    
    private Long inviteUid;
    
    private Long channelUid;
    
    /**
     * 邀请状态 1--已参与 2--邀请成功 3--已过期 4--被替换
     */
    private Integer status;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Integer tenantId;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}
