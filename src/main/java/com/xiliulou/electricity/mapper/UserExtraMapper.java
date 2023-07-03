package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.UserExtra;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (UserExtra)表数据库访问层
 *
 * @author zzlong
 * @since 2023-07-03 15:08:23
 */
public interface UserExtraMapper extends BaseMapper<UserExtra> {

    /**
     * 通过ID查询单条数据
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserExtra queryById(Long uid);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<UserExtra> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param userExtra 实例对象
     * @return 对象列表
     */
    List<UserExtra> queryAll(UserExtra userExtra);

    /**
     * 新增数据
     *
     * @param userExtra 实例对象
     * @return 影响行数
     */
    int insertOne(UserExtra userExtra);

    /**
     * 修改数据
     *
     * @param userExtra 实例对象
     * @return 影响行数
     */
    int update(UserExtra userExtra);

    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 影响行数
     */
    int deleteById(Long uid);

}
