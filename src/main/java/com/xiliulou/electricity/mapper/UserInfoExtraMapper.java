package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.UserInfoExtra;

public interface UserInfoExtraMapper extends BaseMapper<UserInfoExtra> {

    UserInfoExtra selectByUid(Long uid);

    int insert(UserInfoExtra userInfoExtra);

    int updateByUid(UserInfoExtra userInfoExtra);

    int deleteByUid(Long uid);
    
}
