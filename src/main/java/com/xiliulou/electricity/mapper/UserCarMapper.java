package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.UserCar;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UserCar)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-07 17:35:15
 */
public interface UserCarMapper extends BaseMapper<UserCar> {

    /**
     * 通过ID查询单条数据
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserCar selectByUid(@Param("uid") Long uid);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param userCar 实例对象
     * @return 对象列表
     */
    List<UserCar> selectByQuery(UserCar userCar);

    /**
     * 新增数据
     *
     * @param userCar 实例对象
     * @return 影响行数
     */
    int insertOne(UserCar userCar);

    /**
     * 修改数据
     *
     * @param userCar 实例对象
     * @return 影响行数
     */
    int updateByUid(UserCar userCar);

    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 影响行数
     */
    int deleteByUid(@Param("uid") Long uid);

}
