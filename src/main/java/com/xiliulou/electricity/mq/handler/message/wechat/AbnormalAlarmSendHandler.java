/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.mq.handler.message.wechat;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.ElectricityAbnormalMessageNotify;
import com.xiliulou.electricity.enums.notify.AbnormalAlarmExceptionTypeEnum;
import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import com.xiliulou.electricity.mq.handler.message.wechat.AbstractWechatOfficialAccountSendHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * description: 故障上报消息处理器
 *
 * @author caobotao.cbt
 * @date 2024/6/27 20:42
 */
@Component
public class AbnormalAlarmSendHandler extends AbstractWechatOfficialAccountSendHandler {
    
    
    @Override
    protected Map<String, String> converterParamMap(String data) {
        ElectricityAbnormalMessageNotify notify = JsonUtil.fromJson(data, ElectricityAbnormalMessageNotify.class);
        
        Integer exceptionType = notify.getExceptionType();
        
        Map<String, String> params = new HashMap<>();
        
        Optional<AbnormalAlarmExceptionTypeEnum> byType = AbnormalAlarmExceptionTypeEnum.getByType(exceptionType);
        AbnormalAlarmExceptionTypeEnum typeEnum = byType.orElseThrow();
        params.put("first", typeEnum.getFirstName());
        params.put("keyword1", notify.getEquipmentNumber());
        params.put("keyword2", notify.getDescription());
        params.put("keyword3", notify.getReportTime());
        params.put("remark", "为不影响正常使用，请及时核实并处理");
        return params;
    }
    
    @Override
    public Integer getType() {
        return SendMessageTypeEnum.ABNORMAL_ALARM_NOTIFY.getType();
    }
}
