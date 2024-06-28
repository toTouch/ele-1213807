/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.mq.handler.message;


import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.message.MessageCenterConfig;
import com.xiliulou.electricity.dto.message.SendDTO;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;

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
        
        SendDTO sendDTO = this.getSendDTO(mqNotifyCommon);
        if (Objects.isNull(sendDTO)) {
            log.warn("sendDTO is null!");
            return;
        }
        
        sendDTO.setTenantId(mqNotifyCommon.getTenantId());
        if (StringUtils.isBlank(sendDTO.getMessageId())) {
            sendDTO.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        }
        
        ResponseEntity<String> responseEntity = restTemplateService.postJsonForResponseEntity(messageCenterConfig.getUrl(), JsonUtil.toJson(sendDTO), null);
        
        this.afterPostProcessing(sendDTO, responseEntity);
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
    protected void afterPostProcessing(SendDTO sendDTO, ResponseEntity<String> responseEntity) {
        if (Objects.isNull(responseEntity)) {
            log.warn("send warn to message center warn! failure warn send note result is null, messageId={}", sendDTO.getMessageId());
        }
        
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.warn("send warn to message center warn! failure warn send note error, sessionId={}, msg = {}", sendDTO.getMessageId(), responseEntity.getBody());
        }
    }
    
}
