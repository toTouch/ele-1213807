package com.xiliulou.electricity.request.notify;

import com.xiliulou.electricity.entity.notify.MessageCenterSendReceiver;
import lombok.Data;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/6/19 15:05
 * @desc 消息中心请求实体
 */

@Data
public class MessageCenterRequest<T> {
    /**
     * 消息模板编码
     */
    private String messageTemplateCode;
    
    /**
     * 消息id
     */
    private String messageId;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 消息参数
     */
    private T paramMap;
    
    /**
     * 接收人
     */
    private List<MessageCenterSendReceiver> sendReceiverList;
}
