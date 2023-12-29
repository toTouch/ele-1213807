package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.EleHardwareFailureCabinetMsg;
import com.xiliulou.electricity.entity.EleHardwareFailureWarnMsg;
import com.xiliulou.electricity.mapper.EleHardwareFailureCabinetMsgMapper;
import com.xiliulou.electricity.queryModel.failureAlarm.EleHardwareFailureWarnMsgQueryModel;
import com.xiliulou.electricity.service.EleHardwareFailureCabinetMsgService;
import com.xiliulou.electricity.service.EleHardwareFailureWarnMsgService;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    
    @Override
    public void createFailureWarnData() {
        EleHardwareFailureWarnMsgQueryModel queryModel = this.getQueryModel();
        log.error("Hardware Failure CabinetMsg task start params={}", queryModel);
        List<EleHardwareFailureWarnMsgVo> failureWarnMsgList = failureWarnMsgService.list(queryModel);
        log.error("Hardware Failure CabinetMsg task queryWarn: {}", failureWarnMsgList);
        if (ObjectUtils.isEmpty(failureWarnMsgList)) {
            log.error("Hardware Failure CabinetMsg task is empty");
        }
    
        Map<Integer, EleHardwareFailureCabinetMsg> cabinetMsgMap = failureWarnMsgList.stream().collect(
                Collectors.groupingBy(EleHardwareFailureWarnMsgVo::getCabinetId, Collectors.collectingAndThen(Collectors.toList(), e -> this.getCabinetFailureWarnMsg(e))));
        
        log.error("failure warn res={}", JsonUtil.toJson(cabinetMsgMap));
        if (ObjectUtils.isNotEmpty(cabinetMsgMap)) {
            List<EleHardwareFailureCabinetMsg> failureCabinetMsgList = cabinetMsgMap.values().parallelStream().collect(Collectors.toList());
            failureCabinetMsgMapper.batchInsert(failureCabinetMsgList);
        }
    
    }
    
    private EleHardwareFailureCabinetMsg getCabinetFailureWarnMsg(List<EleHardwareFailureWarnMsgVo> failureWarnMsgVoList) {
        EleHardwareFailureCabinetMsg failureCabinetMsg = new EleHardwareFailureCabinetMsg();
        failureWarnMsgVoList.forEach(item -> {
            if (ObjectUtils.isEmpty(failureCabinetMsg.getTenantId())) {
                failureCabinetMsg.setCabinetId(item.getCabinetId());
                failureCabinetMsg.setTenantId(item.getTenantId());
                failureCabinetMsg.setCreateTime(System.currentTimeMillis());
            }
            
            if (Objects.equals(item.getType(), EleHardwareFailureWarnMsg.FAILURE)) {
                failureCabinetMsg.setFailureCount(item.getFailureWarnNum());
            }
            
            if (Objects.equals(item.getType(), EleHardwareFailureWarnMsg.WARN)) {
                failureCabinetMsg.setWarnCount(item.getFailureWarnNum());
            }
        });
        return failureCabinetMsg;
    }
    
    /**
     * 每天凌晨一点默认查询昨天的告警数据
     * @return
     */
    private EleHardwareFailureWarnMsgQueryModel getQueryModel() {
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
        
        EleHardwareFailureWarnMsgQueryModel queryModel = this.getQueryModel();
        queryModel.setStartTime(startTime);
        queryModel.setEndTime(endTime);
        
        return queryModel;
    }
}
