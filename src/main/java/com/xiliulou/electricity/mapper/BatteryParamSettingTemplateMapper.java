package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.BatteryParamSettingTemplate;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (BatteryParamSettingTemplate)表数据库访问层
 *
 * @author Hardy
 * @since 2023-03-29 09:20:22
 */
public interface BatteryParamSettingTemplateMapper extends BaseMapper<BatteryParamSettingTemplate> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryParamSettingTemplate queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<BatteryParamSettingTemplate> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param batteryParamSettingTemplate 实例对象
     * @return 对象列表
     */
    List<BatteryParamSettingTemplate> queryAll(BatteryParamSettingTemplate batteryParamSettingTemplate);
    
    /**
     * 新增数据
     *
     * @param batteryParamSettingTemplate 实例对象
     * @return 影响行数
     */
    int insertOne(BatteryParamSettingTemplate batteryParamSettingTemplate);
    
    /**
     * 修改数据
     *
     * @param batteryParamSettingTemplate 实例对象
     * @return 影响行数
     */
    int update(BatteryParamSettingTemplate batteryParamSettingTemplate);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    List<BatteryParamSettingTemplate> queryList(@Param("offset") Long offset, @Param("size") Long size,
            @Param("name") String name, @Param("tenantId") Integer tenantId);
    
    Long queryCount(@Param("name") String name, @Param("tenantId") Integer tenantId);
}
