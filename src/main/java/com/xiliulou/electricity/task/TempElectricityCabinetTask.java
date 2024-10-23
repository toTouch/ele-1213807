package com.xiliulou.electricity.task;

import cn.hutool.core.thread.ThreadUtil;
import com.xiliulou.electricity.constant.EleCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.response.QueryDeviceDetailResult;
import com.xiliulou.iot.service.RegisterDeviceService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 柜机密钥初始化
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024/10/23 15:12
 */
@Component
@Slf4j
@JobHandler(value = "tempElectricityCabinetTask")
public class TempElectricityCabinetTask extends IJobHandler {
    
    @Autowired
    private ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    private RegisterDeviceService registerDeviceService;
    
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            long offset = 0;
            long size = 300;
            
            while (true) {
                ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().pattern(EleCabinetConstant.ALI_IOT_PATTERN).size(size).offset(offset).build();
                List<ElectricityCabinet> electricityCabinets = electricityCabinetService.selectByQuery(cabinetQuery);
                if (CollectionUtils.isEmpty(electricityCabinets)) {
                    break;
                }
                
                for (ElectricityCabinet electricityCabinet : electricityCabinets) {
                    QueryDeviceDetailResult deviceDetailResult = registerDeviceService.queryDeviceDetail(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
                    if (Objects.isNull(deviceDetailResult)) {
                        log.warn("ELE WARN!not found deviceDetailResult,p={},d={}", electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
                        continue;
                    }
                    
                    ElectricityCabinet update = new ElectricityCabinet();
                    update.setId(electricityCabinet.getId());
                    update.setDeviceSecret(deviceDetailResult.getDeviceSecret());
                    update.setUpdateTime(System.currentTimeMillis());
                    electricityCabinetService.update(update);
                    
                    ThreadUtil.safeSleep(1000L);
                }
                
                offset += size;
            }
        } catch (Exception e) {
            log.error("init deviceSecret fail", e);
        } return IJobHandler.SUCCESS;
    }
}
