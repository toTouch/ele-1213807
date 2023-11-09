package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.vo.ElectricityCabinetExtendDataVO;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * @author: zhangyongbo
 * @Date: 2023/11/07 10:42
 * @Description:
 */
@Service(value = ElectricityIotConstant.NORMAL_ELE_CABINET_SIGNAL_HANDLER)
@Slf4j
public class NormalEleCabinetSignalHandlerIot extends AbstractElectricityIotHandler {
    
    @Autowired
    RedisService redisService;
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        //获取柜机的扩展参数
        ElectricityCabinetExtendDataVO extendsDataVO = JsonUtil.fromJson(receiverMessage.getOriginContent(), ElectricityCabinetExtendDataVO.class);
        if (Objects.nonNull(extendsDataVO) && Objects.nonNull(extendsDataVO.getNetType())) {
            redisService.saveWithString(CacheConstant.CACHE_ELECTRICITY_CABINET_EXTEND_DATA + electricityCabinet.getId(), extendsDataVO.getNetType(), 30L, TimeUnit.MINUTES);
        }
    }
}
