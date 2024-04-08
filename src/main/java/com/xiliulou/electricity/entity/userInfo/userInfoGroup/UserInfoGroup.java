package com.xiliulou.electricity.entity.userInfo.userInfoGroup;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 用户分组表
 * @date 2024/4/8 20:44:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_info_group")
public class UserInfoGroup {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 分组单号
     */
    private String groupNo;
    
    /**
     * 分组名称
     */
    private String name;
    
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
}
