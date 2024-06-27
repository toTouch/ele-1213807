/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.converter.notify;

import com.xiliulou.electricity.entity.RentRefundAuditMessageNotify;
import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * description:退租审核
 *
 * @author caobotao.cbt
 * @date 2024/6/27 14:25
 */
@Component
public class RentRefundAuditDataConverterImpl implements SendWechatNotifyDataConverter<RentRefundAuditMessageNotify> {
    
    @Override
    public Map<String, String> converterParamMap(RentRefundAuditMessageNotify data) {
        Map<String, String> params = new HashMap<>();
        params.put("first", "您好，有用户提交了退租申请");
        params.put("keyword1", data.getUserName());
        params.put("keyword2", data.getBusinessCode());
        params.put("keyword3", data.getApplyTime());
        params.put("remark", "请您尽快审核");
        return params;
    }
    
    @Override
    public String converterTemplateCode() {
        return null;
    }
    
    @Override
    public SendMessageTypeEnum getType() {
        return SendMessageTypeEnum.REFUND_RENT_AUDIT_NOTIFY;
    }
}
