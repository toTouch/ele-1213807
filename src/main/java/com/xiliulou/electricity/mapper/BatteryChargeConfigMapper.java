package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.BatteryChargeConfig;

import java.util.List;

import com.xiliulou.electricity.query.BatteryChargeConfigQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (BatteryChargeConfig)表数据库访问层
 *
 * @author zzlong
 * @since 2022-08-12 14:49:37
 */
public interface BatteryChargeConfigMapper extends BaseMapper<BatteryChargeConfig> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryChargeConfig selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<BatteryChargeConfig> selectByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param batteryChargeConfig 实例对象
     * @return 对象列表
     */
    List<BatteryChargeConfig> selectByQuery(BatteryChargeConfig batteryChargeConfig);

    /**
     * 新增数据
     *
     * @param batteryChargeConfig 实例对象
     * @return 影响行数
     */
    int insertOne(BatteryChargeConfig batteryChargeConfig);

    /**
     * 修改数据
     *
     * @param batteryChargeConfig 实例对象
     * @return 影响行数
     */
    int update(BatteryChargeConfig batteryChargeConfig);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    int insertOrUpdate(BatteryChargeConfig batteryChargeConfig);
}
