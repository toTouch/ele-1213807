package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutorService;


/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service(value= ElectricityIotConstant.NORMAL_ELE_EXCHANGE_HANDLER)
@Slf4j
public class NormalEleExchangeHandlerIot extends AbstractElectricityIotHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    ExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("normalEleExchangeHandlerExecutor",2,"NORMAL_ELE_EXCHANGE_HANDLER_EXECUTOR");

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        executorService.execute(() -> {
            if (StringUtils.isEmpty(receiverMessage.getVersion())) {
                return;
            }

            //版本号修改
            ElectricityCabinet newElectricityCabinet = new ElectricityCabinet();
            newElectricityCabinet.setId(electricityCabinet.getId());
            newElectricityCabinet.setVersion(receiverMessage.getVersion());
            if (electricityCabinetService.update(newElectricityCabinet) > 0) {
                redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + newElectricityCabinet.getId());
                redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName()+electricityCabinet.getTenantId());
            }
            log.error("type is exchange_cabinet,{}", receiverMessage.getOriginContent());
        });
    }

}
