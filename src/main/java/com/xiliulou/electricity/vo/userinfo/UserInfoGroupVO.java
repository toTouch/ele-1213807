package com.xiliulou.electricity.vo.userinfo;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 用户分组
 * @date 2024/4/9 10:37:41
 */
@Data
public class UserInfoGroupVO {
    
    private Long id;
    
    private String groupName;
    
    private Integer userCount;
    
    private Long franchiseeId;
    
    private String franchiseeName;
    
    private Long operator;
    
    private String operatorName;
    
    private Long operatorTime;
    
    private Long createTime;
}
