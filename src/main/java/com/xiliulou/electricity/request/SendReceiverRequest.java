package com.xiliulou.electricity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendReceiverRequest {
    
    /**
     * 发送渠道: 取决SendChannelTypeEnum
     */
    private Integer sendChannel;
    
    /**
     * 接受者
     */
    private Set<String> receiver;
    
    /**
     * 回调地址
     */
    private String callBackUrl;
    
    /**
     * 回调需要返回的参数，入参是什么就返回什么
     */
    private Map<String, Object> callBackMap;
}
