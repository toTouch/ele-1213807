/**
 * Create date: 2024/6/27
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
 * description: 退租审核通知
 *
 * @author caobotao.cbt
 * @date 2024/6/27 21:01
 */
@Component
public class RentalPackageFreezeAuditSendHandler extends AbstractWechatOfficialAccountSendHandler {
    
    
    @Override
    protected Map<String, String> converterParamMap(String data) {
        AuthenticationAuditMessageNotify notify = JsonUtil.fromJson(data, AuthenticationAuditMessageNotify.class);
        
        Map<String, String> params = new HashMap<>();
        params.put("first", "你好，有暂停套餐需要处理");
        params.put("keyword1", notify.getUserName());
        params.put("keyword2", "套餐冻结审核");
        params.put("keyword3", notify.getAuthTime());
        params.put("remark", "请您尽快审核");
        return params;
    }
    
    @Override
    public Integer getType() {
        return SendMessageTypeEnum.RENTAL_PACKAGE_FREEZE_AUDIT_NOTIFY.getType();
    }
}
