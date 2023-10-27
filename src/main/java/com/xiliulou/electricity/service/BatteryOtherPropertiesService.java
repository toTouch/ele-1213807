package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryOtherProperties;

import java.util.List;

/**
 * (City)表服务接口
 *
 * @author makejava
 * @since 2021-01-21 18:05:41
 */
public interface BatteryOtherPropertiesService {

    void insertOrUpdate(BatteryOtherProperties batteryOtherProperties);

    R queryBySn(String sn);
    
    List<BatteryOtherProperties> listBatteryOtherPropertiesBySn(List<String> snList);

    BatteryOtherProperties selectByBatteryName(String sn);
}
