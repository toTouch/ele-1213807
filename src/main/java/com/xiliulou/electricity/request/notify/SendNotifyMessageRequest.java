/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/26
 */

package com.xiliulou.electricity.request.notify;

import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description: 发送通知请求
 *
 * @author caobotao.cbt
 * @date 2024/6/26 18:08
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendNotifyMessageRequest<T> {
    
    /**
     * 发送的消息类型
     */
    private SendMessageTypeEnum type;
    
    /**
     * 时间
     */
    private Long time;
    
    /**
     * 电话
     */
    private String phone;
    
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 消息内容
     */
    private T data;
}
