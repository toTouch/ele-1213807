package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.Role;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (Role)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-09 14:34:00
 */
public interface RoleMapper  extends BaseMapper<Role>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Role queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Role> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @return 对象列表
     */
    List<Role> queryAll();

    /**
     * 新增数据
     *
     * @param role 实例对象
     * @return 影响行数
     */
    int insertOne(Role role);

    /**
     * 修改数据
     *
     * @param role 实例对象
     * @return 影响行数
     */
    int update(Role role);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<Role> queryListByCondition(@Param("offset") Integer offset, @Param("size")Integer size);

    List<Role> queryByRoleIds(List<Long> roleIds);
}