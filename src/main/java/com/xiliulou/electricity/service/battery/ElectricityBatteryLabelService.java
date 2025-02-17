package com.xiliulou.electricity.service.battery;

import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.battery.ElectricityBatteryLabel;

import java.util.List;

/**
 * @author: SJP
 * @create: 2025-02-14 15:41
 **/
public interface ElectricityBatteryLabelService {
    
    void insert(ElectricityBattery battery);
    
    void batchInsert(List<ElectricityBattery> batteries);
    
    int updateById(ElectricityBatteryLabel batteryLabel);
    
    /**
     * 设置预修改标签到缓存中，供后续更新标签时取用
     * @param eId 柜机id
     * @param cellNo 仓门号
     * @param sn 电池sn
     * @param preLabel 预修改标签，枚举值参照com.xiliulou.electricity.enums.battery.BatteryLabelEnum
     */
    void setPreLabel(Integer eId, Integer cellNo, String sn, Integer preLabel);
    
    
}
