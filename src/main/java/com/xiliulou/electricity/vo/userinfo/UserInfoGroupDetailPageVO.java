package com.xiliulou.electricity.vo.userinfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户分组详情
 * @date 2024/4/9 10:37:41
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserInfoGroupDetailPageVO {
    
    private Long id;
    
    private Long uid;
    
    private String userName;
    
    private String phone;
    
    private Long franchiseeId;
    
    private String franchiseeName;
    
    private List<UserInfoGroupIdAndNameVO> groups;
    
    private Long createTime;
    
    private Long updateTime;
    
}
