/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.handler.message.MessageSendHandler;
import com.xiliulou.electricity.mq.handler.message.MessageSendHandlerFactory;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * description: 消息中心消息发送Consumer
 *
 * @author caobotao.cbt
 * @date 2024/6/27 19:15
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.MESSAGE_SEND_SAAS_TOPIC, consumerGroup = MqConsumerConstant.MESSAGE_SEND_SAAS_GROUP, consumeThreadMax = 3)
public class MessageSendConsumer implements RocketMQListener<MessageExt> {
    
    private final MessageSendHandlerFactory messageSendHandlerFactory;
    
    public MessageSendConsumer(MessageSendHandlerFactory messageSendHandlerFactory) {
        this.messageSendHandlerFactory = messageSendHandlerFactory;
    }
    
    
    @Override
    public void onMessage(MessageExt message) {
        try {
            
            if (Objects.isNull(message.getBody())) {
                log.warn("body is empty! messageId:{}", message.getMsgId());
                return;
            }
            
            String body = new String(message.getBody());
            
            log.info("messageId :{}, body:{}", message.getMsgId(), body);
            
            MqNotifyCommon mqNotifyCommon = JsonUtil.fromJson(body, MqNotifyCommon.class);
            
            // 设置链路id
            this.setTraceId(mqNotifyCommon);
            
            // 查找handler
            MessageSendHandler handler = this.findHandler(mqNotifyCommon);
            
            if (Objects.isNull(handler)) {
                return;
            }
            
            handler.sendMessage(mqNotifyCommon);
            
        } catch (Exception e) {
            log.error("MessageSendConsumer.onMessage consumer error ！", e);
        } finally {
            TtlTraceIdSupport.clear();
        }
    }
    
    
    /**
     * 查找handler
     *
     * @param mqNotifyCommon
     * @author caobotao.cbt
     * @date 2024/6/28 10:02
     */
    private MessageSendHandler findHandler(MqNotifyCommon mqNotifyCommon) {
        Integer type = mqNotifyCommon.getType();
        
        MessageSendHandler messageSendHandler = messageSendHandlerFactory.getHandlerByType(type);
        
        if (Objects.isNull(messageSendHandler)) {
            log.warn("message type={} ,Not found handler", type);
            return null;
        }
        
        return messageSendHandler;
    }
    
    /**
     * 设置链路id
     *
     * @param mqNotifyCommon
     * @author caobotao.cbt
     * @date 2024/6/28 10:01
     */
    private void setTraceId(MqNotifyCommon mqNotifyCommon) {
        // 设置链路id
        String traceId = mqNotifyCommon.getTraceId();
        if (StringUtils.isBlank(traceId)) {
            TtlTraceIdSupport.set();
        } else {
            TtlTraceIdSupport.set(traceId);
        }
    }
}
