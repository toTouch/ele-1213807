package com.xiliulou.electricity.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: SendRequest
 * @description: 发送消息请求体
 * @author: renhang
 * @create: 2024-05-25 11:17
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendDTO {
    
    /**
     * 唯一消息id
     */
    private String messageId;
    
    
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    
    /**
     * 消息模板code
     */
    private String messageTemplateCode;
    
    
    /**
     * 占位符的替换值
     */
    private Map<String, String> paramMap;
    
    
    /**
     * 发送渠道和接收者
     */
    private List<SendReceiverDTO> sendReceiverList;
    
    
    /**
     * 回调地址
     */
    private String callBackUrl;
    
    /**
     * 回调需要返回的参数，入参是什么就返回什么
     */
    private Map<String, Object> callBackMap;
}
