package com.xiliulou.electricity.request.userinfo.userInfoGroup;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;

/**
 * @author HeYafeng
 * @description 会员详情-加入分组
 * @date 2024/4/8 19:45:12
 */

@Data
public class UserInfoBindGroupRequest {
    
    @NotNull(message = "uid不能为空")
    private Long uid;
    
    private Long franchiseeId;
    
    private List<Long> groupIds;
    
    /**
     * key: franchiseeId value: groupIds
     */
    private HashMap<Long, List<Long>> franchiseeIdAndGroupIds;
}
    
