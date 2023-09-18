package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.UserSourceQuery;
import com.xiliulou.electricity.vo.UserSourceVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (User)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-27 11:19:50
 */
public interface UserMapper extends BaseMapper<User> {

    User selectById(@Param("uid") Long uid);

    List<User> queryListUserByCriteria(@Param("uid") Long uid, @Param("size") Long size, @Param("offset") Long offset, @Param("name") String name, @Param("phone") String phone, @Param("type") Integer type, @Param("startTime") Long startTime, @Param("endTime") Long endTime,@Param("tenantId") Integer tenantId);

	Integer queryCount(@Param("uid") Long uid, @Param("name") String name, @Param("phone") String phone, @Param("type") Integer type, @Param("startTime") Long startTime, @Param("endTime") Long endTime,@Param("tenantId") Integer tenantId);

    int updateUserByUid(User updateUser);

    Integer updateUserSource(User user);

    List<UserSourceVO> selectUserSourceByPage(UserSourceQuery userSourceQuery);

    Integer selectUserSourcePageCount(UserSourceQuery userSourceQuery);

    User queryByUserName(@Param("username") String username);
}
