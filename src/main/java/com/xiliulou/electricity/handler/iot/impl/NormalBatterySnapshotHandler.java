package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.EleBatterySnapshot;
import com.xiliulou.electricity.entity.EleCabinetCoreData;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.EleBatterySnapshotService;
import com.xiliulou.electricity.service.EleCabinetCoreDataService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 柜机核心板上报数据处理
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-07-06-11:21
 */
@Service(value = ElectricityIotConstant.NORMAL_BATTERY_SNAPSHOT_HANDLER)
@Slf4j
public class NormalBatterySnapshotHandler extends AbstractElectricityIotHandler {
    
    @Autowired
    private EleBatterySnapshotService eleBatterySnapshotService;
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        BatterySnapshotRequest batterySnapshotRequest = JsonUtil.fromJson(receiverMessage.getOriginContent(),
                BatterySnapshotRequest.class);
        eleBatterySnapshotService.insert(EleBatterySnapshot.builder().createTime(batterySnapshotRequest.getReportTime())
                .jsonBatteries(batterySnapshotRequest.getBatteryJson()).eId(electricityCabinet.getId()).build());
    }
    
    
}

@Data
class BatterySnapshotRequest {
    
    private String batteryJson;
    
    private Long reportTime;
}
