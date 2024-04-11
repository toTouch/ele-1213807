package com.xiliulou.electricity.vo.userinfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 用户分组详情表
 * @date 2024/4/8 20:44:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoGroupDetailVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 分组单号
     */
    private String groupNo;
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    private Long groupId;
    
    private String groupName;
}
