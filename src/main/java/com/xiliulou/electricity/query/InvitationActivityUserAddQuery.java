package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 新增邀请人活动
 * @date 2023/11/13 12:50:56
 */
@Data
@Builder
public class InvitationActivityUserAddQuery {
    
    /**
     * 活动ID
     */
    private Long id;
    
    /**
     * 可参与活动的套餐
     */
    private List<Long> memberCardIds;
    
}
