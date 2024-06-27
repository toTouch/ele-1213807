/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/26
 */

package com.xiliulou.electricity.converter.notify;

import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;

import java.util.Map;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/26 18:49
 */
public interface SendWechatNotifyDataConverter<T> {
    
    
    /**
     * 参数转换
     *
     * @param data
     * @author caobotao.cbt
     * @date 2024/6/26 19:01
     */
    Map<String, String> converterParamMap(T data);
    
    
    /**
     * 获取模版编码
     *
     * @author caobotao.cbt
     * @date 2024/6/26 20:40
     */
    String converterTemplateCode();
    
    /**
     * 获取消息发送类型
     *
     * @author caobotao.cbt
     * @date 2024/6/26 19:37
     */
    SendMessageTypeEnum getType();
    
    

}