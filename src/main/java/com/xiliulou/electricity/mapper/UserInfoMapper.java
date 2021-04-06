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
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    UserInfo queryById(Long id);

    /**
     * 查询指定行数据
     */
    IPage queryList(Page page, @Param("query") UserInfoQuery userInfoQuery);



    /**
     * 解除绑定
     *
     * @param userInfo 实例对象
     * @return 影响行数
     */
    int unBind(UserInfo userInfo);

    int minCount(@Param("id") Long id, @Param("updateTime") Long updateTime);

    List<HashMap<String, String>> homeThreeTotal(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay);

    List<HashMap<String, String>> homeThreeService(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay);

    List<HashMap<String, String>> homeThreeMemberCard(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay);

    Integer homeOneMemberCard(@Param("startTimeMilliDay") Long first, @Param("endTimeMilliDay") Long now);

    void updateByUid(UserInfo userInfo);

    void plusCount(@Param("id") Long id, @Param("updateTime") Long updateTime);

    void updateRefund(UserInfo userInfo);

    IPage queryUserInfoList(Page page,@Param("query") UserInfoQuery userInfoQuery);
}