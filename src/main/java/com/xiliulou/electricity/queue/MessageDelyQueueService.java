package com.xiliulou.electricity.queue;

import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于redis zset的延迟队列
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-11-05-15:45
 */
@Slf4j
@Component
public class MessageDelyQueueService {
    
    @Autowired
    private RedisService redisService;
    
    /**
     * 发送消息
     *
     * @param queue   队列名称
     * @param message 消息
     * @param dely    延迟多久（秒）
     * @return
     */
    public Boolean pushMessage(String queue, Message message, int dely) {
        long score = System.currentTimeMillis() + dely * 1000;
        String msg = JsonUtil.toJson(message);
        return redisService.zsetAddString(queue, msg, score);
    }
    
    /**
     * 拉取最新的消息
     *
     * @param queue 队列名称
     * @return
     */
    public List<Message> pullMessage(String queue) {
        
        List<Message> messageList = Lists.newArrayList();
        
        try {
            Set<String> strings = redisService.getZsetStringByRange(queue, 0, System.currentTimeMillis());
            if (CollectionUtils.isEmpty(strings)) {
                return null;
            }
            
            messageList = strings.stream().map(item -> {
                Message message = null;
                
                try {
                    message = JsonUtil.fromJson(item, Message.class);
                } catch (Exception e) {
                    log.error("MESSAG EDELY QUEUE ERROR! json parse message error,queueName={},msg={}", queue, item);
                }
                
                return message;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("MESSAGE DELY QUEUE ERROR! pull dely queue message error,ex={}", e);
        }
        
        return messageList;
    }
    
    
    public Boolean remove(String queue, Message message) {
        //TODO
        //        Long remove = redisTemplate.opsForZSet().remove(queue, JSONObject.toJSONString(message));
        //        return remove>0?Boolean.TRUE:Boolean.FALSE;
        return Boolean.FALSE;
    }
    
    
}
