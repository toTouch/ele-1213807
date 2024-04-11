package com.xiliulou.electricity.vo.userinfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 用户分组名称
 * @date 2024/4/9 10:37:41
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserInfoGroupIdAndNameVO {
    
    private Long groupId;
    
    private String groupNo;
    
    private String groupName;
}
