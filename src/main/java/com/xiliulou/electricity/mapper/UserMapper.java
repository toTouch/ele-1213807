package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (User)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-27 11:19:50
 */
public interface UserMapper extends BaseMapper<User> {



    List<User> queryListUserByCriteria(@Param("uid") Long uid, @Param("size") Long size, @Param("offset") Long offset, @Param("name") String name, @Param("phone") String phone, @Param("type") Integer type, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

}
