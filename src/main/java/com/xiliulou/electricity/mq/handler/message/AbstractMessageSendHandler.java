/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.mq.handler.message;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.message.MessageCenterConfig;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.request.SendMessageRequest;
import com.xiliulou.electricity.service.MsgPlatformRetrofitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.UUID;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/27 19:26
 */
@Slf4j
public abstract class AbstractMessageSendHandler implements MessageSendHandler {
    
    @Resource
    private MsgPlatformRetrofitService msgPlatformRetrofitService;
    
    @Autowired
    private MessageCenterConfig messageCenterConfig;
    
    
    @Override
    public void sendMessage(MqNotifyCommon mqNotifyCommon) {
        
        // 获取发送参数
        SendMessageRequest sendMessageRequest = this.getSendRequest(mqNotifyCommon);
        if (Objects.isNull(sendMessageRequest)) {
            log.warn("sendDTO is null!");
            return;
        }
        
        // 设置租户id
        sendMessageRequest.setTenantId(mqNotifyCommon.getTenantId());
        
        if (StringUtils.isBlank(sendMessageRequest.getMessageId())) {
            // 设置消息id
            sendMessageRequest.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        }
        
        if (StringUtils.isBlank(sendMessageRequest.getMessageTemplateCode())) {
            //设置模版编码
            sendMessageRequest.setMessageTemplateCode(this.getMessageTemplateCode());
        }
        
        // 参数校验
        if (!this.checkParam(sendMessageRequest)) {
            return;
        }
        
        // 发送前处理
        if (!this.preProcessing(sendMessageRequest)) {
            return;
        }
        
        // 发送
        R r = msgPlatformRetrofitService.sendMessage(sendMessageRequest);
        
        //发送后处理
        this.postProcessing(sendMessageRequest, r);
    }
    
    
    /**
     * 参数校验
     *
     * @param sendMessageRequest
     * @author caobotao.cbt
     * @date 2024/7/1 18:25
     */
    protected boolean checkParam(SendMessageRequest sendMessageRequest) {
        Integer tenantId = sendMessageRequest.getTenantId();
        if (Objects.isNull(tenantId)) {
            log.warn("AbstractMessageSendHandler.checkParam tenantId isNull");
            return false;
        }
        
        if (StringUtils.isBlank(sendMessageRequest.getMessageId())) {
            log.warn("AbstractMessageSendHandler.checkParam messageId is isBlank");
            return false;
        }
        
        if (StringUtils.isBlank(sendMessageRequest.getMessageTemplateCode())) {
            log.warn("AbstractMessageSendHandler.checkParam messageTemplateCode is isBlank");
            return false;
        }
        
        if (MapUtils.isEmpty(sendMessageRequest.getParamMap())) {
            log.warn("AbstractMessageSendHandler.checkParam paramMap is isEmpty");
            return false;
        }
        
        if (CollectionUtils.isEmpty(sendMessageRequest.getSendReceiverList())) {
            log.warn("AbstractMessageSendHandler.checkParam sendReceiverList is isEmpty");
            return false;
        }
        
        return true;
        
    }
    
    /**
     * 发送前处理
     *
     * @param sendMessageRequest
     * @author caobotao.cbt
     * @date 2024/6/28 16:46
     */
    protected boolean preProcessing(SendMessageRequest sendMessageRequest) {
        return true;
    }
    
    
    /**
     * 获取消息模版编号
     *
     * @author caobotao.cbt
     * @date 2024/6/28 16:26
     */
    protected String getMessageTemplateCode() {
        return messageCenterConfig.getMessageTemplateCode().get(getType());
    }
    
    
    /**
     * @param mqNotifyCommon
     * @author caobotao.cbt
     * @date 2024/6/27 19:58
     */
    protected abstract SendMessageRequest getSendRequest(MqNotifyCommon mqNotifyCommon);
    
    
    /**
     * 后置处理
     *
     * @param sendMessageRequest
     * @param responseEntity
     * @author caobotao.cbt
     * @date 2024/6/27 16:19
     */
    protected void postProcessing(SendMessageRequest sendMessageRequest, R responseEntity) {
        if (Objects.isNull(responseEntity)) {
            log.warn("send warn to message center warn! failure warn send note result is null, messageId={}", sendMessageRequest.getMessageId());
        }
        log.info("AbstractMessageSendHandler.postProcessing send result:{}", responseEntity.isSuccess());
    }
    
}
