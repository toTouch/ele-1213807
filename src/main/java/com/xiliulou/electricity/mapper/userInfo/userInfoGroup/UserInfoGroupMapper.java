package com.xiliulou.electricity.mapper.userInfo.userInfoGroup;

import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupIdAndNameBO;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.query.UserInfoGroupQuery;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupIdAndNameVO;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户分组
 * @date 2024/4/8 20:31:46
 */
public interface UserInfoGroupMapper {
    
    UserInfoGroup queryByName(@Param("userGroupName") String userGroupName, @Param("tenantId") Integer tenantId);
    
    Integer insertOne(UserInfoGroup userInfoGroup);
    
    List<UserInfoGroupBO> selectPage(UserInfoGroupQuery query);
    
    Integer selectCount(UserInfoGroupQuery query);
    
    List<UserInfoGroupIdAndNameBO> selectAllGroup(UserInfoGroupQuery query);
    
    UserInfoGroup selectById(Long id);
    
    List<UserInfoGroupBO> selectListByIds(@Param("ids") List<Long> ids);
    
    int update(UserInfoGroup oldUserInfo);
    
}
