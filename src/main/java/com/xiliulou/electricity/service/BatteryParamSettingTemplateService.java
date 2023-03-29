package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryParamSettingTemplate;
import com.xiliulou.electricity.query.BatteryParamSettingTemplateQuery;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (BatteryParamSettingTemplate)表服务接口
 *
 * @author Hardy
 * @since 2023-03-29 09:20:22
 */
public interface BatteryParamSettingTemplateService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryParamSettingTemplate queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryParamSettingTemplate queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<BatteryParamSettingTemplate> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param batteryParamSettingTemplate 实例对象
     * @return 实例对象
     */
    BatteryParamSettingTemplate insert(BatteryParamSettingTemplate batteryParamSettingTemplate);
    
    /**
     * 修改数据
     *
     * @param batteryParamSettingTemplate 实例对象
     * @return 实例对象
     */
    Integer update(BatteryParamSettingTemplate batteryParamSettingTemplate);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    Triple<Boolean, String, Object> queryList(Long offset, Long size, String name);
    
    Triple<Boolean, String, Object> queryCount(String name);
    
    Triple<Boolean, String, Object> deleteOne(Long id);
    
    Triple<Boolean, String, Object> saveOne(BatteryParamSettingTemplateQuery query);
    
    Triple<Boolean, String, Object> updateOne(BatteryParamSettingTemplateQuery query);
}
