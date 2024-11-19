/**
 *  Create date: 2024/6/28
 */

package com.xiliulou.electricity.mq.handler.message.mail;

import com.google.common.collect.Maps;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.EmailRecipient;
import com.xiliulou.electricity.entity.MQMailMessageNotify;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.entity.notify.NotifyUserInfo;
import com.xiliulou.electricity.mq.handler.message.AbstractMessageSendHandler;
import com.xiliulou.electricity.request.SendMessageRequest;
import com.xiliulou.electricity.request.SendReceiverRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * description: 邮件发送处理器
 *
 * @author caobotao.cbt
 * @date 2024/6/28 15:00
 */
@Slf4j
public abstract class AbstractMailSendHandler extends AbstractMessageSendHandler {
    
    
    public static final Integer WECHAT_SEND_MAIL = 2;
    
//    private static final Map<String, String> HEARDERS = Maps.newHashMapWithExpectedSize(1);
//
//    static {
//        HEARDERS.put("Accept-Encoding", "gzip, deflate");
//    }
    
    
    @Override
    public SendMessageRequest getSendRequest(MqNotifyCommon mqNotifyCommon) {
        
        if (Objects.isNull(mqNotifyCommon.getData())) {
            log.warn("AbstractMailSendHandler.getSendDTO data is null");
            return null;
        }
        
        MQMailMessageNotify notify = JsonUtil.fromJson(JsonUtil.toJson(mqNotifyCommon.getData()), MQMailMessageNotify.class);
        
        Set<String> receiver = notify.getTo().stream().map(e -> e.getEmail()).collect(Collectors.toSet());
        
        //获取模版参数
        Map<String, String> map = converterParamMap(notify);
        
        if (MapUtils.isEmpty(map)) {
            return null;
        }
    
        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.setParamMap(map);
        SendReceiverRequest receiverRequest = new SendReceiverRequest();
        receiverRequest.setSendChannel(WECHAT_SEND_MAIL);
        receiverRequest.setReceiver(receiver);
        sendMessageRequest.setSendReceiverList(Collections.singletonList(receiverRequest));
        return sendMessageRequest;
        
    }
    
    /**
     * 获取参数转换
     *
     * @param notify
     * @author caobotao.cbt
     * @date 2024/6/28 15:05
     */
    protected Map<String, String> converterParamMap(MQMailMessageNotify notify) {
        HashMap<String, String> map = Maps.newHashMapWithExpectedSize(1);
        map.put("version", notify.getSubject());
        map.put("content", notify.getText());
        return map;
    }
    
    
//    @Override
//    protected Map<String, String> getHeaders(SendDTO sendDTO) {
//        return this.HEARDERS;
//    }
    
}
