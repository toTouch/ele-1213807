package com.xiliulou.electricity.task.installment;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    
    private RedisService redisService;
    
    private InstallmentRecordService installmentRecordService;
    
    @Scheduled(fixedRate = 60 * 1000, initialDelay = 60 * 1000)
    public void cancelSign() {
        double now = System.currentTimeMillis();
        double min = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        
        Set<String> zSetStringByRange = redisService.getZsetStringByRange(CACHE_INSTALLMENT_CANCEL_SIGN, min, now);
        if (CollectionUtils.isEmpty(zSetStringByRange)) {
            return;
        }

        for (String externalAgreementNo : zSetStringByRange) {
            try {
                installmentRecordService.cancel(externalAgreementNo);
            } catch (Exception e) {
                log.error("Installment Cancel Task error! externalAgreementNo={}", externalAgreementNo, e);
            }
        }
        
        redisService.removeZsetRangeByScore(CACHE_INSTALLMENT_CANCEL_SIGN, min, now);
    }
}
