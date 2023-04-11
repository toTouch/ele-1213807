package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.AppParamSettingTemplate;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (AppParamSettingTemplate)表数据库访问层
 *
 * @author Hardy
 * @since 2023-03-30 19:53:09
 */
public interface AppParamSettingTemplateMapper extends BaseMapper<AppParamSettingTemplate> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    AppParamSettingTemplate queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<AppParamSettingTemplate> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param appParamSettingTemplate 实例对象
     * @return 对象列表
     */
    List<AppParamSettingTemplate> queryAll(AppParamSettingTemplate appParamSettingTemplate);
    
    /**
     * 新增数据
     *
     * @param appParamSettingTemplate 实例对象
     * @return 影响行数
     */
    int insertOne(AppParamSettingTemplate appParamSettingTemplate);
    
    /**
     * 修改数据
     *
     * @param appParamSettingTemplate 实例对象
     * @return 影响行数
     */
    int update(AppParamSettingTemplate appParamSettingTemplate);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    List<AppParamSettingTemplate> queryList(@Param("size") Long size, @Param("offset") Long offset,
            @Param("tenantId") Integer tenantId);
    
    Long queryCount(@Param("tenantId") Integer tenantId);
}
