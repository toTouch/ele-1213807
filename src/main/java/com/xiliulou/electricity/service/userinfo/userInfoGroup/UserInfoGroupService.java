package com.xiliulou.electricity.service.userinfo.userInfoGroup;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.user.UserGroupSaveRequest;

/**
 * @author HeYafeng
 * @description 用户分组
 * @date 2024/4/8 19:59:04
 */
public interface UserInfoGroupService {
    
    R save(UserGroupSaveRequest userGroupSaveRequest, Long uid);
}
