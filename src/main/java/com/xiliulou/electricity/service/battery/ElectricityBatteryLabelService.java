package com.xiliulou.electricity.service.battery;

import com.xiliulou.electricity.dto.battery.BatteryLabelModifyDto;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.battery.ElectricityBatteryLabel;
import com.xiliulou.electricity.vo.battery.ElectricityBatteryLabelVO;

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
     * @param labelModifyDto 预修改标签及操作人uid
     */
    void setPreLabel(Integer eId, String cellNo, String sn, BatteryLabelModifyDto labelModifyDto);
    
    List<ElectricityBatteryLabel> listBySns(List<String> sns);
    
    List<ElectricityBatteryLabelVO> listLabelVOBySns(List<String> sns);
}
