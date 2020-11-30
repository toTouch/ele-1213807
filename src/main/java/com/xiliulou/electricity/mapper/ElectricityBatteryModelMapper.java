package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityBatteryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 电池型号(ElectricityBatteryModel)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 14:44:44
 */
public interface ElectricityBatteryModelMapper extends BaseMapper<ElectricityBatteryModel> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityBatteryModel queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ElectricityBatteryModel> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param ElectricityBatteryModel 实例对象
     * @return 对象列表
     */
    List<ElectricityBatteryModel> queryAll(ElectricityBatteryModel ElectricityBatteryModel);

    /**
     * 新增数据
     *
     * @param ElectricityBatteryModel 实例对象
     * @return 影响行数
     */
    int insertOne(ElectricityBatteryModel ElectricityBatteryModel);

    /**
     * 修改数据
     *
     * @param ElectricityBatteryModel 实例对象
     * @return 影响行数
     */
    int update(ElectricityBatteryModel ElectricityBatteryModel);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    List<ElectricityBatteryModel> getElectricityBatteryModelPage(@Param("offset") Long offset, @Param("size") Long size, @Param("name") String name);
}