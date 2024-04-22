package com.xiliulou.electricity.entity.userinfo.userInfoGroup;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 用户分组详情修改记录表
 * @date 2024/4/15 09:19:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_info_group_detail_history")
public class UserInfoGroupDetailHistory {
    
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
     * 变更后分组ID
     */
    private String newGroupIds;
    
    /**
     * 操作账号
     */
    private Long operator;
    
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
    
    /**
     * 操作类型:0-其它 1-退押
     */
    private Integer type;
}
