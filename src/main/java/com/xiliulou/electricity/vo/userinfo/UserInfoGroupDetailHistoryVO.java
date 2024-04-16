package com.xiliulou.electricity.vo.userinfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 用户分组详情修改记录
 * @date 2024/4/15 09:19:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoGroupDetailHistoryVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 变更前分组ID
     */
    private String oldGroupIds;
    
    /**
     * 变更前分组名称
     */
    private String oldGroupNames;
    
    /**
     * 变更后分组ID
     */
    private String newGroupIds;
    
    /**
     * 变更后分组名称
     */
    private String newGroupNames;
    
    /**
     * 操作人
     */
    private Long operator;
    
    /**
     * 操作账号
     */
    private String operatorName;
    
    /**
     * 更新时间
     */
    private Long operatorTime;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
}
