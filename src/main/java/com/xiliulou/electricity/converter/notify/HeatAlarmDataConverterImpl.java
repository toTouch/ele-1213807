/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.converter.notify;

import com.xiliulou.electricity.entity.EleHighTemperatureAlarmNotify;
import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * description: 高危预警参数转换
 *
 * @author caobotao.cbt
 * @date 2024/6/27 13:55
 */
@Component
public class HeatAlarmDataConverterImpl implements SendWechatNotifyDataConverter<EleHighTemperatureAlarmNotify> {
    
    @Override
    public Map<String, String> converterParamMap(EleHighTemperatureAlarmNotify data) {
        Map<String, String> params = new HashMap<>();
        
        params.put("first", "您好，您的柜机温度过高");
        params.put("keyword1", data.getCabinetName() + "(" + data.getCellNo() + "号仓)");
        params.put("keyword2", String.format("%.1f", data.getCellHeat()));
        params.put("keyword3", data.getReportTime());
        params.put("remark", "请您及时处理");
        return params;
    }
    
    @Override
    public String converterTemplateCode() {
        return null;
    }
    
    @Override
    public SendMessageTypeEnum getType() {
        return SendMessageTypeEnum.HIGH_WARNING_NOTIFY;
    }
}
