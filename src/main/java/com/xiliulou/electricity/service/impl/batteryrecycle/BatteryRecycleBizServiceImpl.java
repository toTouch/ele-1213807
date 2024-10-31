package com.xiliulou.electricity.service.impl.batteryrecycle;

import com.google.common.collect.Maps;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.asset.ElectricityCabinetBO;
import com.xiliulou.electricity.constant.CabinetBoxConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.BatteryRecycleDelayDTO;
import com.xiliulou.electricity.entity.BoxOtherProperties;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.batteryrecycle.BatteryRecycleRecord;
import com.xiliulou.electricity.enums.LockReasonEnum;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.mq.producer.BatteryRecycleProducer;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.service.BoxOtherPropertiesService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.batteryRecycle.BatteryRecycleBizService;
import com.xiliulou.electricity.service.batteryRecycle.BatteryRecycleRecordService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2024/10/31 10:17
 * @desc
 */
@Service
@Slf4j
public class BatteryRecycleBizServiceImpl implements BatteryRecycleBizService {
    
    @Resource
    private BatteryRecycleRecordService batteryRecycleRecordService;
    
    @Resource
    private ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private BoxOtherPropertiesService boxOtherPropertiesService;
    
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    
    @Resource
    private BatteryRecycleProducer batteryRecycleProducer;
    
    public void doBatteryRecycle(Integer tenantId) {
        // 检测是否存在状态为已录入的数据
        BatteryRecycleRecord record = batteryRecycleRecordService.listFirstNotLockedRecord(tenantId);
        if (Objects.isNull(record)) {
            log.info("battery recycle info! data is empty");
            return;
        }
        
        Long maxId = record.getId() - NumberConstant.ONE_L;
        Long size = 200L;
        while (true) {
            List<BatteryRecycleRecord> batteryRecycleRecords = batteryRecycleRecordService.listNotLockedRecord(tenantId, maxId, size);
            if (ObjectUtils.isEmpty(batteryRecycleRecords)) {
                return;
            }
            
            List<String> snList = batteryRecycleRecords.parallelStream().map(BatteryRecycleRecord::getSn).collect(Collectors.toList());
            List<ElectricityCabinetBox> electricityCabinetBoxes = electricityCabinetBoxService.listBySnList(snList);
            if (ObjectUtils.isEmpty(electricityCabinetBoxes)) {
                return;
            }
            
            Map<String, ElectricityCabinetBox> boxMap = electricityCabinetBoxes.stream()
                    .collect(Collectors.toMap(ElectricityCabinetBox::getSn, electricityCabinetBox -> electricityCabinetBox, (v1, v2) -> v2));
            
            List<Integer> cabinetIdList = electricityCabinetBoxes.parallelStream().map(ElectricityCabinetBox::getElectricityCabinetId).collect(Collectors.toList());
            List<ElectricityCabinetBO> electricityCabinets = electricityCabinetService.listByIdList(cabinetIdList);
            if (ObjectUtils.isEmpty(electricityCabinets)) {
                log.warn("battery recycle warn! not find cabinet, ids: {}", cabinetIdList);
                return;
            }
            
            Map<Integer, ElectricityCabinetBO> cabinetMap = electricityCabinets.stream()
                    .collect(Collectors.toMap(ElectricityCabinetBO::getId, electricityCabinet -> electricityCabinet, (v1, v2) -> v2));
            
            batteryRecycleRecords.stream().forEach(batteryRecycleRecord -> {
                // 不在仓
                if (!boxMap.containsKey(batteryRecycleRecord.getSn())) {
                    return;
                }
                
                // 查询柜机是否存在
                ElectricityCabinetBox electricityCabinetBox = boxMap.get(batteryRecycleRecord.getSn());
                if (Objects.isNull(cabinetMap.get(electricityCabinetBox.getElectricityCabinetId()))) {
                    return;
                }
    
                ElectricityCabinetBO electricityCabinetBO = cabinetMap.get(electricityCabinetBox.getElectricityCabinetId());
                ElectricityCabinet electricityCabinet = new ElectricityCabinet();
                BeanUtils.copyProperties(electricityCabinetBO, electricityCabinet);
                
                // 修改锁仓原因
                BoxOtherProperties boxOtherProperties = BoxOtherProperties.builder().electricityCabinetId(electricityCabinet.getId()).cellNo(electricityCabinetBox.getCellNo())
                        .remark(batteryRecycleRecord.getRecycleReason()).lockReason(LockReasonEnum.OTHER.getCode()).build();
                boxOtherPropertiesService.insertOrUpdate(boxOtherProperties);
    
                EleOuterCommandQuery eleOuterCommandQuery = new EleOuterCommandQuery();
                eleOuterCommandQuery.setCommand(ElectricityIotConstant.ELE_COMMAND_CELL_UPDATE);
                eleOuterCommandQuery.setProductKey(electricityCabinet.getProductKey());
                eleOuterCommandQuery.setDeviceName(electricityCabinet.getDeviceName());
                
                HashMap<String, Object> dataMap = Maps.newHashMap();
                dataMap.put("lockType", CabinetBoxConstant.LOCK_BY_USER);
                dataMap.put("isForbidden", true);
                dataMap.put("lockReason", CabinetBoxConstant.LOCK_REASON_OTHER);
                List<String> cellList = new ArrayList<>();
                cellList.add(electricityCabinetBox.getCellNo());
                dataMap.put("cell_list", cellList);
                eleOuterCommandQuery.setData(dataMap);
    
                //发送锁仓命令
                R r = electricityCabinetService.sendCommand(eleOuterCommandQuery);
                if (!r.isSuccess()) {
                    log.warn("BATTERY RECYCLE LOCK CELL WARN! send command warn! sn:{}, msg:{}", r.getErrMsg());
                    return;
                }
                
                // 发送异步消息
                BatteryRecycleDelayDTO batteryRecycleDelayDTO = BatteryRecycleDelayDTO.builder().sn(batteryRecycleRecord.getSn()).cabinetId(electricityCabinet.getId())
                        .recycleId(batteryRecycleRecord.getId()).cellNo(electricityCabinetBox.getCellNo()).build();
                batteryRecycleProducer.sendDelayMessage(batteryRecycleDelayDTO);
            });
            
            maxId = batteryRecycleRecords.get(batteryRecycleRecords.size() - 1).getId();
        }
    }
}
