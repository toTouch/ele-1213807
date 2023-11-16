package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author HeYafeng
 * @description 新增邀请人活动
 * @date 2023/11/13 12:50:56
 */
@Data
@Builder
public class InvitationActivityUserSaveQuery implements Serializable {
    
    /**
     * 邀请人uid
     */
    private Long uid;
    
    /**
     * 活动id列表
     */
    private List<Long> activityIds;
    
}
