/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.mq.handler.message.wechat;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.EleHighTemperatureAlarmNotify;
import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import com.xiliulou.electricity.mq.handler.message.wechat.AbstractWechatOfficialAccountSendHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/27 20:58
 */
@Component
public class HeatAlarmSendHandler extends AbstractWechatOfficialAccountSendHandler {
    
    @Override
    protected String getMessageTemplateCode() {
        return null;
    }
    
    @Override
    protected Map<String, String> converterParamMap(String data) {
        EleHighTemperatureAlarmNotify notify = JsonUtil.fromJson(data, EleHighTemperatureAlarmNotify.class);
        Map<String, String> params = new HashMap<>();
        
        params.put("first", "您好，您的柜机温度过高");
        params.put("keyword1", notify.getCabinetName() + "(" + notify.getCellNo() + "号仓)");
        params.put("keyword2", String.format("%.1f", notify.getCellHeat()));
        params.put("keyword3", notify.getReportTime());
        params.put("remark", "请您及时处理");
        return params;
    }
    
    @Override
    public Integer getType() {
        return SendMessageTypeEnum.HIGH_WARNING_NOTIFY.getType();
    }
}
