package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.UserDataScope;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UserDataScope)表数据库访问层
 *
 * @author zzlong
 * @since 2022-09-19 14:22:34
 */
public interface UserDataScopeMapper extends BaseMapper<UserDataScope> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    UserDataScope selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<UserDataScope> selectByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param userDataScope 实例对象
     * @return 对象列表
     */
    List<UserDataScope> selectByQuery(UserDataScope userDataScope);

    /**
     * 新增数据
     *
     * @param userDataScope 实例对象
     * @return 影响行数
     */
    int insertOne(UserDataScope userDataScope);

    /**
     * 修改数据
     *
     * @param userDataScope 实例对象
     * @return 影响行数
     */
    int update(UserDataScope userDataScope);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer batchInsert(List<UserDataScope> userDataScopes);

    Integer deleteByUid(@Param("uid") Long uid);

    List<UserDataScope> selectByUid(Long uid);

    List<Long> selectDataIdByUid(Long uid);
}
