/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/26
 */

package com.xiliulou.electricity.converter.notify;

import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * description: 参数转换工厂
 *
 * @author caobotao.cbt
 * @date 2024/6/26 19:32
 */
@Component
public class SendWechatNotifyDataConverterFactory<T> implements BeanPostProcessor {
    
    private final Map<Integer, SendWechatNotifyDataConverter<T>> map = new HashMap<>();
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SendWechatNotifyDataConverter) {
            SendWechatNotifyDataConverter<T> converter = (SendWechatNotifyDataConverter) bean;
            SendMessageTypeEnum type = converter.getType();
            Assert.notNull(type, "" + SendWechatNotifyDataConverter.class + "#getType is null");
            Assert.isTrue(!map.containsKey(type.getType()), "" + SendWechatNotifyDataConverter.class + " type:" + type.getType() + " repeat ");
            map.put(type.getType(), converter);
        }
        return bean;
    }
    
    
    /**
     * 根据类型获取转换类
     *
     * @param type
     * @author caobotao.cbt
     * @date 2024/6/26 20:27
     */
    public SendWechatNotifyDataConverter<T> getConverterByType(Integer type) {
        return map.get(type);
    }
    
    
}
