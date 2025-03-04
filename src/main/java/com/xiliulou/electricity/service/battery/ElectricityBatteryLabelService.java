package com.xiliulou.electricity.service.battery;

import com.xiliulou.electricity.dto.battery.BatteryLabelModifyDTO;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.battery.ElectricityBatteryLabel;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.vo.battery.ElectricityBatteryLabelVO;

import java.util.List;
import java.util.Map;

/**
 * @author: SJP
 * @create: 2025-02-14 15:41
 **/
public interface ElectricityBatteryLabelService {
    
    void insert (ElectricityBatteryLabel batteryLabel);
    
    void insertWithBattery(ElectricityBattery battery);
    
    void batchInsert(List<ElectricityBattery> batteries, Long operatorUid);
    
    ElectricityBatteryLabel selectBySnAndTenantId(String sn, Integer tenantId);
    
    int updateById(ElectricityBatteryLabel batteryLabel);
    
    void deleteBySnAndTenantId(String sn, Integer tenantId);
    
    /**
     * 设置预修改标签到缓存中，供后续更新标签时取用
     * @param eId 柜机id
     * @param cellNo 仓门号
     * @param sn 电池sn
     * @param labelModifyDto 预修改标签及操作人uid
     */
    void setPreLabel(Integer eId, String cellNo, String sn, BatteryLabelModifyDTO labelModifyDto);
    
    List<ElectricityBatteryLabel> listBySns(List<String> sns);
    
    /**
     * 清除领用数据
     */
    int deleteReceivedData(String sn);
    
    List<ElectricityBatteryLabelVO> listLabelVOBySns(List<String> sns, Map<String, Integer> snAndLabel);
    
    Integer countReceived(Long uid);
    
    /**
     * 保存或者清除柜机格挡表内的锁定在仓sn
     */
    void updateLockSnAndBatteryLabel(EleOuterCommandQuery eleOuterCommandQuery, ElectricityCabinet electricityCabinet, Long operatorId);
    
    void updateOpenCellAndBatteryLabel(EleOuterCommandQuery eleOuterCommandQuery, ElectricityCabinet electricityCabinet, Long operatorId, List<ElectricityCabinetBox> electricityCabinetBoxList);
}
