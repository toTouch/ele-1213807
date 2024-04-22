package com.xiliulou.electricity.query.userinfo.userInfoGroup;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户分组详情历史记录查询
 * @date 2024/4/9 10:26:47
 */
@Builder
@Data
public class UserInfoGroupDetailHistoryQuery {
    
    private Long size;
    
    private Long offset;
    
    private Long uid;
    
    private Integer tenantId;
    
    private List<Long> franchiseeIds;
    
}
