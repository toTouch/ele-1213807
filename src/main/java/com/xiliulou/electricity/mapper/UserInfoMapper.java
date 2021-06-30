package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.UserInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * 用户列表(TUserInfo)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
public interface UserInfoMapper extends BaseMapper<UserInfo> {


    /**
     * 查询指定行数据
     */
    List<UserInfo> queryList( @Param("query") UserInfoQuery userInfoQuery);


    List<HashMap<String, String>> homeThreeTotal(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay,@Param("tenantId")Integer tenantId);


    Integer homeOneTotal(@Param("first") Long first, @Param("now") Long now,@Param("tenantId")Integer tenantId);

}
