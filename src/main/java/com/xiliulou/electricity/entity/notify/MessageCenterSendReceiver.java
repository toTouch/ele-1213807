package com.xiliulou.electricity.entity.notify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/6/19 15:22
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageCenterSendReceiver<T> {
    /**
     * 接收者
     */
    private List<String> receiver;
    
    /**
     * 消息发送渠道：1.短信 2 邮件
     */
    private Integer sendChannel;
    
    /**
     * 回调地址
     */
    private String callBackUrl;
    
    /**
     * 回调参数
     */
    private T callBackMap;
}
