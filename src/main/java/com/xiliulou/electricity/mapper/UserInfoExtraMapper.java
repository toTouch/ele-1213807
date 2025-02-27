package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.UserInfoExtra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * (UserInfoExtra)表数据库访问层
 *
 * @author zzlong
 * @since 2024-02-18 10:39:59
 */
public interface UserInfoExtraMapper extends BaseMapper<UserInfoExtra> {

    UserInfoExtra selectByUid(Long uid);

    int insert(UserInfoExtra userInfoExtra);

    int updateByUid(UserInfoExtra userInfoExtra);

    int deleteByUid(Long uid);
    
    Integer updateUserNotActivityByUid(@Param("uid") Long uid, @Param("updateTime") Long updateTime);
}
