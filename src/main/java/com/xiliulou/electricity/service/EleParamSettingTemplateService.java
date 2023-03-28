package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleParamSettingTemplate;

import java.util.List;

/**
 * (EleParamSettingTemplate)表服务接口
 *
 * @author Hardy
 * @since 2023-03-28 09:53:18
 */
public interface EleParamSettingTemplateService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EleParamSettingTemplate queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleParamSettingTemplate queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EleParamSettingTemplate> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param eleParamSettingTemplate 实例对象
     * @return 实例对象
     */
    EleParamSettingTemplate insert(EleParamSettingTemplate eleParamSettingTemplate);
    
    /**
     * 修改数据
     *
     * @param eleParamSettingTemplate 实例对象
     * @return 实例对象
     */
    Integer update(EleParamSettingTemplate eleParamSettingTemplate);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
}
