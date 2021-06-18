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
    IPage queryList(Page page, @Param("query") UserInfoQuery userInfoQuery);




    List<HashMap<String, String>> homeThreeTotal(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay);

    List<HashMap<String, String>> homeThreeAuth(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay);


    void updateRefund(UserInfo userInfo);

    IPage queryUserInfoList(Page page,@Param("query") UserInfoQuery userInfoQuery);

    Integer homeOneTotal(@Param("first") Long first, @Param("now") Long now);

    Integer homeOneAuth(@Param("first") Long first, @Param("now") Long now);
}
