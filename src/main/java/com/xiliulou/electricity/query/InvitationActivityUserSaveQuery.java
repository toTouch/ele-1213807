package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 新增邀请人活动
 * @date 2023/11/13 12:50:56
 */
@Data
@Builder
public class InvitationActivityUserSaveQuery {
    
    /**
     * 活动ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long uid;
    
    /**
     * 可参与活动的套餐
     */
    private List<Long> memberCardIds;
    
}
