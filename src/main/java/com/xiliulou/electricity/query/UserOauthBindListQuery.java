package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author HeYafeng
 * @date 2025/1/6 19:32:27
 */
@Builder
@Data
public class UserOauthBindListQuery {
    
    private Integer tenantId;
    
    private Long uid;
    
    private String phone;
}
