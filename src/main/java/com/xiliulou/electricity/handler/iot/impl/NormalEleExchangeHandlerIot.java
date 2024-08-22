package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.EleCabinetConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetExtra;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ElectricityCabinetExtraService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.utils.DeviceTextUtil;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.ExecutorService;


/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service(value = ElectricityIotConstant.NORMAL_ELE_EXCHANGE_HANDLER)
@Slf4j
public class NormalEleExchangeHandlerIot extends AbstractElectricityIotHandler {
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private ElectricityCabinetExtraService electricityExtraService;
    
    ExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("normalEleExchangeHandlerExecutor", 2, "NORMAL_ELE_EXCHANGE_HANDLER_EXECUTOR");
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        executorService.execute(() -> {
            Integer eid = electricityCabinet.getId();
            //版本号修改
            if (StringUtils.isNotEmpty(receiverMessage.getVersion())) {
                ElectricityCabinet newElectricityCabinet = new ElectricityCabinet();
                newElectricityCabinet.setId(eid);
                newElectricityCabinet.setVersion(receiverMessage.getVersion());
                
                //柜机模式修改
                newElectricityCabinet.setPattern(EleCabinetConstant.IOT_PATTERN);
                
                if (electricityCabinetService.update(newElectricityCabinet) > 0) {
                    redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + newElectricityCabinet.getId());
                    redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
                }
            }
    
            // 更新柜机参数
            NormalEleExchangeMsg normalEleExchangeMsg = JsonUtil.fromJson(receiverMessage.getOriginContent(), NormalEleExchangeMsg.class);
            if (Objects.isNull(normalEleExchangeMsg)) {
                log.error("PARSE ELE EXCHANGE MSG ERROR! sessionId={}, eid={}", receiverMessage.getSessionId(), eid);
                return;
            }
            
            ElectricityCabinetExtra cabinetFromCache = electricityExtraService.queryByEidFromCache(Long.valueOf(eid));
            ElectricityCabinetExtra electricityCabinetExtra = new ElectricityCabinetExtra();
            electricityCabinetExtra.setEid(eid.longValue());
            Integer batteryCountType = null;
            
            if (Objects.nonNull(normalEleExchangeMsg.getBatSta())) {
                if (!Objects.equals(cabinetFromCache.getBatteryCountType(), normalEleExchangeMsg.getBatSta())) {
                    batteryCountType = normalEleExchangeMsg.getBatSta();
                }
            } else {
                // 低于2.1.8的版本，不支持少电多电参数上报，修改状态为正常
                batteryCountType = EleCabinetConstant.BATTERY_COUNT_TYPE_NORMAL;
            }
    
            if (Objects.nonNull(batteryCountType)) {
                electricityCabinetExtra.setBatteryCountType(batteryCountType);
                electricityExtraService.update(electricityCabinetExtra);
            }
        });
    }
    
}

@Data
class NormalEleExchangeMsg {
    
    private String type;
    
    /**
     * batSta: 电池状态：0 正常、1 少电、2 多电
     */
    private Integer batSta;
}