/**
 * Create date: 2024/6/28
 */

package com.xiliulou.electricity.mq.handler.message;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * description: 消息发送handler工厂
 *
 * @author caobotao.cbt
 * @date 2024/6/28 08:45
 */
@Component
public class MessageSendHandlerFactory implements BeanPostProcessor {
    
    
    private final Map<Integer, MessageSendHandler> handlerMap = new HashMap<>();
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof MessageSendHandler) {
            MessageSendHandler handler = (MessageSendHandler) bean;
            Integer type = handler.getType();
            Assert.notNull(type, "" + bean.getClass() + ".getType is null");
            Assert.isTrue(!handlerMap.containsKey(type), "" + bean.getClass() + " type:" + type + " exist ");
            handlerMap.put(type, handler);
        }
        return bean;
    }
    
    
    /**
     * 根据类型获handler
     *
     * @param type
     * @author caobotao.cbt
     * @date 2024/6/26 20:27
     */
    public MessageSendHandler getHandlerByType(Integer type) {
        return handlerMap.get(type);
    }
}
