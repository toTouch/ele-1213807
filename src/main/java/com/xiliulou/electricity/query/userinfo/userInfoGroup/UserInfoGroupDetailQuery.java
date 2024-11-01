package com.xiliulou.electricity.query.userinfo.userInfoGroup;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户分组详情查询
 * @date 2024/4/9 10:26:47
 */
@Builder
@Data
public class UserInfoGroupDetailQuery {
    
    private Long size;
    
    private Long offset;
    
    private Long uid;
    
    private Long groupId;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private List<Long> franchiseeIds;
}
