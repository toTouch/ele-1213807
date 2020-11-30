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


}