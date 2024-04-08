package com.xiliulou.electricity.mapper.userInfo.userInfoGroup;

import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroup;
import org.apache.ibatis.annotations.Param;

/**
 * @author HeYafeng
 * @description 用户分组
 * @date 2024/4/8 20:31:46
 */
public interface UserInfoGroupMapper {
    
    UserInfoGroup queryByName(@Param("userGroupName") String userGroupName, @Param("tenantId") Integer tenantId);
    
    Integer insertOne(UserInfoGroup userInfoGroup);
}
