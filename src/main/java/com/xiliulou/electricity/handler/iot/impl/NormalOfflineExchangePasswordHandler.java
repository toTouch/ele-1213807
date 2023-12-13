package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.ElectricityCabinetOtherSetting;
import com.xiliulou.electricity.dto.OtherSettingParamTemplateRequestDTO;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * @author BaoYu
 * @description:
 * @date 2023/12/13 11:31
 */

@Slf4j
@Service(value = ElectricityIotConstant.NORMAL_OFFLINE_EXCHANGE_PASSWORD_HANDLER)
public class NormalOfflineExchangePasswordHandler extends AbstractElectricityIotHandler {
    
    
    @Override
    protected void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("no sessionId,{}", receiverMessage.getOriginContent());
            return ;
        }
        Map<String, Object> map = JsonUtil.fromJson(receiverMessage.getOriginContent(), Map.class);
        
        log.info("offline exchange password ! data={}", map);
    }
    
}
