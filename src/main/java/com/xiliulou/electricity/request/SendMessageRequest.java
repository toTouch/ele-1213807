package com.xiliulou.electricity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    
    /**
     * 消息模板code
     */
    private String messageTemplateCode;
    
    /**
     * 唯一消息id
     */
    private String messageId;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    
    /**
     * 占位符的替换值
     */
    private Map<String, String> paramMap;
    
    
    /**
     * 发送渠道和接收者
     */
    private List<SendReceiverRequest> sendReceiverList;
}
