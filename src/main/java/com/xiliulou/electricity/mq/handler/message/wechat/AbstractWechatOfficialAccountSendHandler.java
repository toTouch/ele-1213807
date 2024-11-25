/**
 * Create date: 2024/6/27
 */

package com.xiliulou.electricity.mq.handler.message.wechat;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.entity.notify.NotifyUserInfo;
import com.xiliulou.electricity.mq.handler.message.AbstractMessageSendHandler;
import com.xiliulou.electricity.request.SendMessageRequest;
import com.xiliulou.electricity.request.SendReceiverRequest;
import com.xiliulou.electricity.service.notify.NotifyUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/27 20:04
 */
@Slf4j
public abstract class AbstractWechatOfficialAccountSendHandler extends AbstractMessageSendHandler {
    
    @Resource
    private NotifyUserInfoService notifyUserInfoService;
    
    
    public static final Integer WECHAT_SEND_CHANNEL = 3;
    
    @Override
    protected SendMessageRequest getSendRequest(MqNotifyCommon mqNotifyCommon) {
        String phone = mqNotifyCommon.getPhone();
        
        if (StringUtils.isBlank(phone)) {
            log.warn("AbstractWechatOfficialAccountSendHandler.getSendDTO phone isBlank");
            return null;
        }
        
        if (Objects.isNull(mqNotifyCommon.getData())) {
            log.warn("AbstractWechatOfficialAccountSendHandler.getSendDTO data isBlank");
            return null;
        }
        
        //数据转换
        Map<String, String> map = converterParamMap(JsonUtil.toJson(mqNotifyCommon.getData()));
        
        if (MapUtils.isEmpty(map)) {
            return null;
        }
        
        NotifyUserInfo notifyUserInfo = notifyUserInfoService.queryFromCacheByPhone(phone);
        if (Objects.isNull(notifyUserInfo)) {
            log.warn("AbstractWechatOfficialAccountSendHandler.getSendDTO phone={} not exist ", phone);
            return null;
        }
    
        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.setParamMap(map);
        SendReceiverRequest receiverRequest = new SendReceiverRequest();
        receiverRequest.setSendChannel(WECHAT_SEND_CHANNEL);
        receiverRequest.setReceiver(Collections.singleton(notifyUserInfo.getOpenId()));
        sendMessageRequest.setSendReceiverList(Collections.singletonList(receiverRequest));
        return sendMessageRequest;
    }
    
    
    /**
     * 参数转换
     *
     * @param data
     * @author caobotao.cbt
     * @date 2024/6/27 20:39
     */
    protected abstract Map<String, String> converterParamMap(String data);
    
}
