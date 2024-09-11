package com.xiliulou.electricity.task.installment;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_CANCEL_SIGN;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/9 14:11
 */
@Component
@AllArgsConstructor
@Slf4j
public class InstallmentCancelTask {
    
    private InstallmentRecordService installmentRecordService;
    
    private RedisTemplate<String, String> redisTemplate;
    
    @Scheduled(fixedRate = 60 * 1000, initialDelay = 60 * 1000)
    public void cancelSign() {
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());
        try {
            double now = System.currentTimeMillis();
            double min = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
            log.info("取消签约定时任务调试，min={}，now={}", min, now);
            
            Map<String, Double> results = getAndDelete(min, now);
            log.info("取消签约定时任务调试，results={}", results);
            if (CollectionUtils.isEmpty(results)) {
                log.info("取消签约定时任务调试，未取到请求签约号");
                return;
            }
            
            results.keySet().forEach(externalAgreementNo -> {
                try {
                    installmentRecordService.cancel(externalAgreementNo);
                } catch (Exception e) {
                    log.error("Installment Cancel Task error! externalAgreementNo={}", externalAgreementNo, e);
                }
            });
        } finally {
            MDC.clear();
        }
    }
    
    private Map<String, Double> getAndDelete(double min, double now) {
        // 定义 Lua 脚本
        String luaScript = "local key = KEYS[1]\n" +
                "local minScore = ARGV[1]\n" +
                "local maxScore = ARGV[2]\n" +
                "local results = redis.call('ZRANGEBYSCORE', key, minScore, maxScore, 'WITHSCORES')\n" +
                "local members = {}\n" +
                "for i = 1, #results, 2 do\n" +
                "   table.insert(members, results[i])\n" +
                "end\n" +
                "redis.call('ZREM', key, unpack(members))\n" +
                "return results";
        
        // 创建 DefaultRedisScript 并设置脚本语言为 Lua
        RedisScript<List> script = new DefaultRedisScript<>(luaScript, List.class);
        List<String> results = redisTemplate.execute(script, Collections.singletonList(CACHE_INSTALLMENT_CANCEL_SIGN), String.valueOf(min), String.valueOf(now));
        
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        
        String mapKey = null;
        double mapValue;
        Map<String, Double> map = new HashMap<>(5);
        for (int i = 0; i < results.size(); i++) {
            if ((i + 1) % 2 != 0) {
                mapKey = results.get(i);
            } else {
                mapValue = Double.parseDouble(results.get(i));
                map.put(mapKey, mapValue);
            }
        }
        
        return map;
    }
}
