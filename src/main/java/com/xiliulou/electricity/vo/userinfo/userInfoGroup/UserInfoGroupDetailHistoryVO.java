package com.xiliulou.electricity.vo.userinfo.userInfoGroup;

import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupIdAndNameBO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
     * 变更前分组
     */
    private List<UserInfoGroupIdAndNameBO> oldGroupList;
    
    /**
     * 变更前分组
     */
    private List<UserInfoGroupIdAndNameBO> newGroupList;
    
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
    
    /**
     * 加盟商名称
     */
    private String franchiseeName;
}
