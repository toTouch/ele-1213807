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
public class UserInfoBindGroupRequestV2 {
    
    /**
     * 用户id
     */
    private Long uid;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 分组id
     */
    private Long groupId;
    
    /**
     * 分组id list
     */
    private List<Long> groupIds;
    
    /**
     * 用户id list
     */
    private List<String> userPhones;
    
    /**
     * key: franchiseeId value: groupIds
     */
    private HashMap<Long, List<Long>> franchiseeIdAndGroupIds;
}

