package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.EleParamSettingTemplate;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (EleParamSettingTemplate)表数据库访问层
 *
 * @author Hardy
 * @since 2023-03-28 09:53:18
 */
public interface EleParamSettingTemplateMapper extends BaseMapper<EleParamSettingTemplate> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EleParamSettingTemplate queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EleParamSettingTemplate> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param eleParamSettingTemplate 实例对象
     * @return 对象列表
     */
    List<EleParamSettingTemplate> queryAll(EleParamSettingTemplate eleParamSettingTemplate);
    
    /**
     * 新增数据
     *
     * @param eleParamSettingTemplate 实例对象
     * @return 影响行数
     */
    int insertOne(EleParamSettingTemplate eleParamSettingTemplate);
    
    /**
     * 修改数据
     *
     * @param eleParamSettingTemplate 实例对象
     * @return 影响行数
     */
    int update(EleParamSettingTemplate eleParamSettingTemplate);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    List<EleParamSettingTemplate> queryList(Long offset, Long size, String name);
    
    Long queryCount(String name);
}
