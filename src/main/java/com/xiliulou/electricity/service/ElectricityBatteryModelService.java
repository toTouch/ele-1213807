package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityBatteryModel;

import java.util.List;

/**
 * 电池型号(ElectricityBatteryModel)表服务接口
 *
 * @author makejava
 * @since 2020-11-26 14:44:44
 */
public interface ElectricityBatteryModelService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityBatteryModel queryByIdFromDB(Integer id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityBatteryModel queryByIdFromCache(Integer id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ElectricityBatteryModel> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param ElectricityBatteryModel 实例对象
     * @return 实例对象
     */
    ElectricityBatteryModel insert(ElectricityBatteryModel ElectricityBatteryModel);

    /**
     * 修改数据
     *
     * @param ElectricityBatteryModel 实例对象
     * @return 实例对象
     */
    Integer update(ElectricityBatteryModel ElectricityBatteryModel);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Integer id);

    ElectricityBatteryModel queryById(Integer modelId);
}