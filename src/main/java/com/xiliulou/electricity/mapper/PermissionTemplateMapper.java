package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.PermissionTemplate;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (PermissionTemplate)表数据库访问层
 *
 * @author zzlong
 * @since 2022-09-19 16:34:06
 */
public interface PermissionTemplateMapper extends BaseMapper<PermissionTemplate> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    PermissionTemplate selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<PermissionTemplate> selectByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param permissionTemplate 实例对象
     * @return 对象列表
     */
    List<PermissionTemplate> selectByQuery(PermissionTemplate permissionTemplate);

    /**
     * 新增数据
     *
     * @param permissionTemplate 实例对象
     * @return 影响行数
     */
    int insertOne(PermissionTemplate permissionTemplate);

    /**
     * 修改数据
     *
     * @param permissionTemplate 实例对象
     * @return 影响行数
     */
    int update(PermissionTemplate permissionTemplate);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    int deleteByType(Integer type);

    int batchInsert(List<PermissionTemplate> permissionList);
}
