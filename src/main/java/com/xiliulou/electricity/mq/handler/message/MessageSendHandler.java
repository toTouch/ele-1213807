/**
 * Create date: 2024/6/27
 */

package com.xiliulou.electricity.mq.handler.message;

import com.xiliulou.electricity.entity.MqNotifyCommon;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/27 19:31
 */
public interface MessageSendHandler {
    
    /**
     * 消息发送
     *
     * @param mqNotifyCommon
     * @author caobotao.cbt
     * @date 2024/6/27 19:32
     */
    void sendMessage(MqNotifyCommon mqNotifyCommon);
    
    
    /**
     * 类型获取
     *
     * @author caobotao.cbt
     * @date 2024/6/27 19:32
     */
    Integer getType();
}