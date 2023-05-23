package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.AppParamSettingTemplate;
import com.xiliulou.electricity.query.AppParamSettingTemplateQuery;

import java.util.List;

/**
 * (AppParamSettingTemplate)表服务接口
 *
 * @author Hardy
 * @since 2023-03-30 19:53:09
 */
public interface AppParamSettingTemplateService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    AppParamSettingTemplate queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    AppParamSettingTemplate queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<AppParamSettingTemplate> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param appParamSettingTemplate 实例对象
     * @return 实例对象
     */
    AppParamSettingTemplate insert(AppParamSettingTemplate appParamSettingTemplate);
    
    /**
     * 修改数据
     *
     * @param appParamSettingTemplate 实例对象
     * @return 实例对象
     */
    Integer update(AppParamSettingTemplate appParamSettingTemplate);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    R queryList(Long size, Long offset);
    
    Long queryCount();
    
    R deleteOne(Long id);
    
    R saveOne(AppParamSettingTemplateQuery query);
    
    R updateOne(AppParamSettingTemplateQuery query);
}
