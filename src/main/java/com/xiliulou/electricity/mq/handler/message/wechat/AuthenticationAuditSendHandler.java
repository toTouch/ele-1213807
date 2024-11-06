/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.mq.handler.message.wechat;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.AuthenticationAuditMessageNotify;
import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import com.xiliulou.electricity.mq.handler.message.wechat.AbstractWechatOfficialAccountSendHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * description: 实名认证
 *
 * @author caobotao.cbt
 * @date 2024/6/27 20:47
 */
@Component
public class AuthenticationAuditSendHandler extends AbstractWechatOfficialAccountSendHandler {
    
    
    @Override
    protected Map<String, String> converterParamMap(String data) {
        AuthenticationAuditMessageNotify notify = JsonUtil.fromJson(data, AuthenticationAuditMessageNotify.class);
        Map<String, String> params = new HashMap<>();
        params.put("first", "您好，有用户提交了实名认证申请");
        params.put("keyword1", notify.getUserName());
        params.put("keyword2", "实名认证审核");
        params.put("keyword3", notify.getAuthTime());
        params.put("remark", "请您尽快审核");
        return params;
    }
    
    @Override
    public Integer getType() {
        return SendMessageTypeEnum.AUTHENTICATION_AUDIT_NOTIFY.getType();
    }
}
