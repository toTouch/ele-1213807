package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBatteryModel;

/**
 * 电池型号(ElectricityBatteryModel)表服务接口
 *
 * @author makejava
 * @since 2020-11-26 14:44:44
 */
public interface ElectricityBatteryModelService {


    R saveElectricityBatteryModel(ElectricityBatteryModel electricityBatteryModel);

    /**
     * @param electricityBatteryModel
     * @return
     */
    R updateElectricityBatteryModel(ElectricityBatteryModel electricityBatteryModel);

    ElectricityBatteryModel getElectricityBatteryModelById(Integer id);
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