/**
 *  Create date: 2024/6/28
 */

package com.xiliulou.electricity.mq.producer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.UUID;


/**
 * description: 消息中心发送producer
 *
 * @author caobotao.cbt
 * @date 2024/6/28 10:18
 */
@Slf4j
@Component
public class MessageSendProducer {
    
    private final RocketMqService rocketMqService;
    
    public MessageSendProducer(RocketMqService rocketMqService) {
        this.rocketMqService = rocketMqService;
    }
    
    /**
     * 异步消息发送
     *
     * @param msg
     * @param tag
     * @param key
     * @param delayLevel
     * @author caobotao.cbt
     * @date 2024/6/28 10:21
     */
    public void sendAsyncMsg(MqNotifyCommon msg, final String tag, final String key, int delayLevel) {
        this.buildTraceId(msg);
        rocketMqService.sendAsyncMsg(MqProducerConstant.MESSAGE_SEND_SAAS_TOPIC, JsonUtil.toJson(msg), tag, key, delayLevel);
    }
    
    /**
     * 同步发送
     *
     * @param query
     * @param tag
     * @param key
     * @param delayLevel
     * @author caobotao.cbt
     * @date 2024/6/28 14:18
     */
    public Pair<Boolean, String> sendSyncMsg(MqNotifyCommon msg, String tag, final String key, int delayLevel) {
        this.buildTraceId(msg);
        return rocketMqService.sendSyncMsg(MqProducerConstant.MESSAGE_SEND_SAAS_TOPIC, JsonUtil.toJson(msg), tag, key, delayLevel);
    }
    
    /**
     * 设置链路id
     *
     * @param msg
     * @author caobotao.cbt
     * @date 2024/6/28 10:24
     */
    private void buildTraceId(MqNotifyCommon msg) {
        msg.setTraceId(TtlTraceIdSupport.get());
        if (StringUtils.isBlank(msg.getTraceId())) {
            msg.setTraceId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        
    }
    
    
}
