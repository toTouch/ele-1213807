/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/26
 */

package com.xiliulou.electricity.converter.notify;

import com.xiliulou.electricity.entity.ElectricityAbnormalMessageNotify;
import com.xiliulou.electricity.enums.notify.AbnormalAlarmExceptionTypeEnum;
import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/26 19:13
 */
@Component
public class AbnormalAlarmDataConverterImpl implements SendWechatNotifyDataConverter<ElectricityAbnormalMessageNotify> {
    
    
    @Override
    public Map<String, String> converterParamMap(ElectricityAbnormalMessageNotify data) {
        
        Integer exceptionType = data.getExceptionType();
        
        Map<String, String> params = new HashMap<>();
        
        Optional<AbnormalAlarmExceptionTypeEnum> byType = AbnormalAlarmExceptionTypeEnum.getByType(exceptionType);
        AbnormalAlarmExceptionTypeEnum typeEnum = byType.orElseThrow();
        params.put("first", typeEnum.getFirstName());
        params.put("keyword1", data.getAddress());
        params.put("keyword2", data.getEquipmentNumber());
        params.put("keyword3", typeEnum.getExceptionName());
        params.put("keyword4", data.getDescription());
        params.put("keyword5", data.getReportTime());
        params.put("remark", "为不影响正常使用，请及时核实并处理");
        return params;
    }
    
    
    @Override
    public String converterTemplateCode() {
        return null;
    }
    
    @Override
    public SendMessageTypeEnum getType() {
        return SendMessageTypeEnum.ABNORMAL_ALARM_NOTIFY;
    }
    
  
}
