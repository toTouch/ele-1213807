package com.xiliulou.electricity.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (BatteryParamConfig)表数据库访问层
 *
 * @author Hardy
 * @since 2023-03-29 09:54:39
 */
public interface BatteryParamConfigMapper extends BaseMapper<BatteryParamConfig> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryParamConfig queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<BatteryParamConfig> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param batteryParamConfig 实例对象
     * @return 对象列表
     */
    List<BatteryParamConfig> queryAll(BatteryParamConfig batteryParamConfig);
    
    /**
     * 新增数据
     *
     * @param batteryParamConfig 实例对象
     * @return 影响行数
     */
    int insertOne(BatteryParamConfig batteryParamConfig);
    
    /**
     * 修改数据
     *
     * @param batteryParamConfig 实例对象
     * @return 影响行数
     */
    int update(BatteryParamConfig batteryParamConfig);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    List<BatteryParamConfig> queryByTemplateId(@Param("tid") Long tid, @Param("tenantId") Integer tenantId);
}
