package com.xiliulou.electricity.handler.iot.impl;

import com.google.gson.annotations.SerializedName;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetExtra;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ElectricityCabinetExtraService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;
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
            if (StringUtils.isEmpty(receiverMessage.getVersion())) {
                return;
            }
            
            //版本号修改
            ElectricityCabinet newElectricityCabinet = new ElectricityCabinet();
            Integer eid = electricityCabinet.getId();
            newElectricityCabinet.setId(eid);
            newElectricityCabinet.setVersion(receiverMessage.getVersion());
            if (electricityCabinetService.update(newElectricityCabinet) > 0) {
                redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + newElectricityCabinet.getId());
                redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
            }
    
            NormalEleExchangeMsg normalEleExchangeMsg = JsonUtil.fromJson(receiverMessage.getOriginContent(), NormalEleExchangeMsg.class);
            if (Objects.isNull(normalEleExchangeMsg)) {
                log.error("PARSE ELE EXCHANGE MSG ERROR! sessionId={}", receiverMessage.getSessionId());
                return;
            }
    
            // 封装柜机扩展参数
            ElectricityCabinetExtra electricityCabinetExtra = ElectricityCabinetExtra.builder().eid(Long.valueOf(eid)).batteryCountType(normalEleExchangeMsg.getBatSta()).build();
            ElectricityCabinet cabinetFromCache = electricityCabinetService.queryByIdFromCache(eid);
            if (Objects.nonNull(cabinetFromCache)) {
                BeanUtils.copyProperties(cabinetFromCache, electricityCabinetExtra);
            }
    
            electricityExtraService.insertOne(electricityCabinetExtra);
        });
    }

}

@Data
class NormalEleExchangeMsg {
    
    private String productKey;
    
    private String sessionId;
    
    private String type;
    
    @SerializedName("update_time")
    private Long updateTime;
    
    private String version;
    
    /**
     * batSta: 电池状态：0 正常、1 少电、2 多电
     */
    private Integer batSta;
}