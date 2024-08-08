package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.ElectricityCabinetChooseCellConfig;

/**
 * @author renhang
 * @date 2023/3/14 舒适换电格挡配置
 */
public interface ElectricityCabinetChooseCellConfigService {
    
    ElectricityCabinetChooseCellConfig queryConfigByNumFromDB(Integer num);
    
    /**
     * 根据编号查询缓存中的机柜选择单元配置
     *
     * @param num 编号
     * @return 返回查询到的配置
     */
    ElectricityCabinetChooseCellConfig queryConfigByNumFromCache(Integer num);
}
