/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.mq.handler.message;


import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.message.MessageCenterConfig;
import com.xiliulou.electricity.dto.message.SendDTO;
import com.xiliulou.electricity.dto.message.SendReceiverDTO;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;

import javax.annotation.Resource;
import java.util.Collections;
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
    
    @Qualifier("restTemplateServiceImpl")
    @Autowired
    RestTemplateService restTemplateService;
    
    @Autowired
    private MessageCenterConfig messageCenterConfig;
    
    
    @Override
    public void sendMessage(MqNotifyCommon mqNotifyCommon) {
        
        // 获取发送参数
        SendDTO sendDTO = this.getSendDTO(mqNotifyCommon);
        if (Objects.isNull(sendDTO)) {
            log.warn("sendDTO is null!");
            return;
        }
        
        // 设置租户id
        sendDTO.setTenantId(mqNotifyCommon.getTenantId());
        
        if (StringUtils.isBlank(sendDTO.getMessageId())) {
            // 设置消息id
            sendDTO.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        }
        
        if (StringUtils.isBlank(sendDTO.getMessageTemplateCode())) {
            //设置模版编码
            sendDTO.setMessageTemplateCode(this.getMessageTemplateCode());
        }
        
        // 发送前处理
        if (!this.preProcessing(sendDTO)) {
            return;
        }
        
        // 发送
        ResponseEntity<String> responseEntity = restTemplateService.postJsonForResponseEntity(messageCenterConfig.getUrl(), JsonUtil.toJson(sendDTO), null);
        
        //发送后处理
        this.postProcessing(sendDTO, responseEntity);
    }
    
    /**
     * 发送前处理
     *
     * @param sendDTO
     * @author caobotao.cbt
     * @date 2024/6/28 16:46
     */
    protected boolean preProcessing(SendDTO sendDTO) {
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
    protected abstract SendDTO getSendDTO(MqNotifyCommon mqNotifyCommon);
    
    
    /**
     * 后置处理
     *
     * @param sendDTO
     * @param responseEntity
     * @author caobotao.cbt
     * @date 2024/6/27 16:19
     */
    protected void postProcessing(SendDTO sendDTO, ResponseEntity<String> responseEntity) {
        if (Objects.isNull(responseEntity)) {
            log.warn("send warn to message center warn! failure warn send note result is null, messageId={}", sendDTO.getMessageId());
        }
        
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.warn("send warn to message center warn! failure warn send note error, sessionId={}, msg = {}", sendDTO.getMessageId(), responseEntity.getBody());
        }
    }
    
}
