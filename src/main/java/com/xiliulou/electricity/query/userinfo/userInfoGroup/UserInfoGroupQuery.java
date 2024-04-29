package com.xiliulou.electricity.query.userinfo.userInfoGroup;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户分组查询
 * @date 2024/4/9 10:26:47
 */
@Builder
@Data
public class UserInfoGroupQuery {
    
    private Long size;
    
    private Long offset;
    
    private Long groupId;
    
    private Integer tenantId;
    
    private List<Long> franchiseeIds;
    
    private String groupName;
}
