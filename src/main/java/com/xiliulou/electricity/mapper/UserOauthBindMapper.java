package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.UserOauthBind;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (UserOauthBind)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-03 09:17:39
 */
public interface UserOauthBindMapper extends BaseMapper<UserOauthBind> {


    List<UserOauthBind> queryListByCondition(@Param("size") Integer size, @Param("offset") Integer offset, @Param("uid") Long uid, @Param("thirdId") String thirdId, @Param("phone") String phone);


    UserOauthBind queryUserOauthBySysId(@Param("uid") Long uid,@Param("tenantId") Integer tenantId);
}
