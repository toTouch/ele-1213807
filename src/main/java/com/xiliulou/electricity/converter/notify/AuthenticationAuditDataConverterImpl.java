/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.converter.notify;

import com.xiliulou.electricity.entity.AuthenticationAuditMessageNotify;
import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * description: 实名认证
 *
 * @author caobotao.cbt
 * @date 2024/6/27 14:36
 */
@Component
public class AuthenticationAuditDataConverterImpl implements SendWechatNotifyDataConverter<AuthenticationAuditMessageNotify>{
    
    @Override
    public Map<String, String> converterParamMap(AuthenticationAuditMessageNotify data) {
        Map<String, String> params = new HashMap<>();
        params.put("first", "您好，有用户提交了实名认证申请");
        params.put("keyword1", data.getUserName());
        params.put("keyword2", data.getBusinessCode());
        params.put("keyword3", data.getAuthTime());
        params.put("remark", "请您尽快审核");
        return params;
    }
    
    @Override
    public String converterTemplateCode() {
        return null;
    }
    
    @Override
    public SendMessageTypeEnum getType() {
        return SendMessageTypeEnum.AUTHENTICATION_AUDIT_NOTIFY;
    }
}
