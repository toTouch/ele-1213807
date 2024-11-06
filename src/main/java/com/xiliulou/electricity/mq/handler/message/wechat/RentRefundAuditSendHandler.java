/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/28
 */

package com.xiliulou.electricity.mq.handler.message.wechat;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.RentRefundAuditMessageNotify;
import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * description: 退租审核通知
 *
 * @author caobotao.cbt
 * @date 2024/6/28 08:40
 */
@Component
public class RentRefundAuditSendHandler extends AbstractWechatOfficialAccountSendHandler {
    
    @Override
    public Integer getType() {
        return SendMessageTypeEnum.REFUND_RENT_AUDIT_NOTIFY.getType();
    }
    
    
    @Override
    protected Map<String, String> converterParamMap(String data) {
        RentRefundAuditMessageNotify notify = JsonUtil.fromJson(data, RentRefundAuditMessageNotify.class);
        Map<String, String> params = new HashMap<>();
        params.put("first", "您好，有用户提交了退租申请");
        params.put("keyword1", notify.getUserName());
        params.put("keyword2", "退租审核");
        params.put("keyword3", notify.getApplyTime());
        params.put("remark", "请您尽快审核");
        return params;
        
    }
}
