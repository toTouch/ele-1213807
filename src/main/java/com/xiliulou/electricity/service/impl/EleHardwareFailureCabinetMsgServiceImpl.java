package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.EleHardwareFailureCabinetMsg;
import com.xiliulou.electricity.entity.EleHardwareFailureWarnMsg;
import com.xiliulou.electricity.handler.iot.impl.HardwareFailureWarnMsgHandler;
import com.xiliulou.electricity.mapper.EleHardwareFailureCabinetMsgMapper;
import com.xiliulou.electricity.queryModel.failureAlarm.EleHardwareFailureWarnMsgQueryModel;
import com.xiliulou.electricity.service.EleHardwareFailureCabinetMsgService;
import com.xiliulou.electricity.service.EleHardwareFailureWarnMsgService;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2023/12/28 16:18
 * @desc 每天定时刷新故障告警的数据：统计每个柜子对应的故障的次数，告警次数
 */

@Service
@Slf4j
public class EleHardwareFailureCabinetMsgServiceImpl implements EleHardwareFailureCabinetMsgService {
    
    @Resource
    private EleHardwareFailureCabinetMsgMapper failureCabinetMsgMapper;
    
    @Resource
    private EleHardwareFailureWarnMsgService failureWarnMsgService;
    
    @Resource(name = ElectricityIotConstant.HARDWARE_FAILURE_WARN_MSG_HANDLER)
    private HardwareFailureWarnMsgHandler failureWarnMsgHandler;
    
    @Override
    public void createFailureWarnData() {
        testHandler();
        EleHardwareFailureWarnMsgQueryModel queryModel = this.getQueryModel();
        List<EleHardwareFailureWarnMsgVo> failureWarnMsgList = failureWarnMsgService.list(queryModel);
        if (ObjectUtils.isEmpty(failureWarnMsgList)) {
            log.error("Hardware Failure CabinetMsg task is empty");
        }
    
        Map<Integer, EleHardwareFailureCabinetMsg> cabinetMsgMap = failureWarnMsgList.stream().collect(
                Collectors.groupingBy(EleHardwareFailureWarnMsgVo::getCabinetId, Collectors.collectingAndThen(Collectors.toList(), e -> this.getCabinetFailureWarnMsg(e, queryModel))));
        
        if (ObjectUtils.isNotEmpty(cabinetMsgMap)) {
            // 删除昨天的历史数据
            failureCabinetMsgMapper.batchDelete(queryModel.getStartTime(), queryModel.getEndTime());
            
            List<EleHardwareFailureCabinetMsg> failureCabinetMsgList = cabinetMsgMap.values().parallelStream().collect(Collectors.toList());
            // 批量插入新的数据
            failureCabinetMsgMapper.batchInsert(failureCabinetMsgList);
        }
    
    }
    
    private void testHandler() {
        ReceiverMessage receiverMessage = new ReceiverMessage();
        receiverMessage.setProductKey("a1QqoBrbcT1");
        receiverMessage.setDeviceName("222");
        receiverMessage.setOriginContent("{\"msgType\":410,\"devId\":\"76\",\"t\":1703832074004,\"txnNo\":\"123456789\",\"alarmList\":[{\"id\":\"112\",\"alarmTime\":1703832074005,\"alarmDesc\":\"00\",\"alarmFlag\":0,\"alarmId\":\"123\",\"boxId\":9,\"type\":1,\"occurNum\":1}]}");
        failureWarnMsgHandler.receiveMessageProcess(receiverMessage);
    }
    
    private EleHardwareFailureCabinetMsg getCabinetFailureWarnMsg(List<EleHardwareFailureWarnMsgVo> failureWarnMsgVoList, EleHardwareFailureWarnMsgQueryModel queryModel) {
        EleHardwareFailureCabinetMsg failureCabinetMsg = new EleHardwareFailureCabinetMsg();
        failureWarnMsgVoList.forEach(item -> {
            if (ObjectUtils.isEmpty(failureCabinetMsg.getTenantId())) {
                failureCabinetMsg.setCabinetId(item.getCabinetId());
                failureCabinetMsg.setTenantId(item.getTenantId());
                failureCabinetMsg.setCreateTime(queryModel.getTime());
            }
            
            if (Objects.equals(item.getType(), EleHardwareFailureWarnMsg.FAILURE)) {
                failureCabinetMsg.setFailureCount(item.getFailureWarnNum());
            }
            
            if (Objects.equals(item.getType(), EleHardwareFailureWarnMsg.WARN)) {
                failureCabinetMsg.setWarnCount(item.getFailureWarnNum());
            }
        });
    
        Optional.ofNullable(failureCabinetMsg.getFailureCount()).ifPresent(item -> {
            failureCabinetMsg.setFailureCount(0);
        });
    
        Optional.ofNullable(failureCabinetMsg.getWarnCount()).ifPresent(item -> {
            failureCabinetMsg.setWarnCount(0);
        });
        
        return failureCabinetMsg;
    }
    
    /**
     * 每天凌晨一点默认查询昨天的告警数据
     * @return
     */
    private EleHardwareFailureWarnMsgQueryModel getQueryModel() {
        EleHardwareFailureWarnMsgQueryModel queryModel = new EleHardwareFailureWarnMsgQueryModel();
    
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        long startTime = calendar.getTimeInMillis();
        
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        
        long endTime = calendar.getTimeInMillis();
    
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        long time = calendar.getTimeInMillis();
    
        queryModel.setStartTime(startTime);
        queryModel.setEndTime(endTime);
        queryModel.setTime(time);
        
        return queryModel;
    }
}
