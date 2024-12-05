package com.xiliulou.electricity.callback.impl.fy;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.query.installment.InstallmentDeductNotifyQuery;
import com.xiliulou.electricity.query.installment.InstallmentDeductionRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentSignNotifyQuery;
import com.xiliulou.electricity.service.FyConfigService;
import com.xiliulou.electricity.service.installment.InstallmentBizService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionRecordService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.utils.InstallmentUtil;
import com.xiliulou.pay.deposit.fengyun.config.FengYunConfig;
import com.xiliulou.pay.deposit.fengyun.utils.FyAesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_CANCEL_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_RECORD_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.NOTIFY_STATUS_SIGN;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/7 22:09
 */
@Component
@AllArgsConstructor
@Slf4j
public class FyInstallmentHandler {
    
    private FengYunConfig fengYunConfig;
    
    private InstallmentRecordService installmentRecordService;
    
    private InstallmentBizService installmentBizService;
    
    private InstallmentDeductionRecordService installmentDeductionRecordService;
    
    private InstallmentDeductionPlanService installmentDeductionPlanService;
    
    private FyConfigService fyConfigService;
    
    private RedisTemplate<String, String> redisTemplate;
    
    
    public String signNotify(String bizContent, Long uid) {
        try {
            String decrypt = FyAesUtil.decrypt(bizContent, fengYunConfig.getAesKey());
            InstallmentSignNotifyQuery signNotifyQuery = JsonUtil.fromJson(decrypt, InstallmentSignNotifyQuery.class);
            
            InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNoWithoutUnpaid(signNotifyQuery.getExternalAgreementNo());
            
            R<String> stringR;
            if (NOTIFY_STATUS_SIGN.equals(Integer.valueOf(signNotifyQuery.getStatus()))) {
                // 处理签约成功回调
                stringR = installmentBizService.handleSign(installmentRecord, signNotifyQuery.getAgreementNo());
            } else {
                // 处理解约成功回调
                stringR = installmentBizService.handleTerminating(installmentRecord);
            }
            
            if (stringR.isSuccess()) {
                return "SUCCESS";
            }
        } catch (Exception e) {
            log.error("INSTALLMENT NOTIFY ERROR! uid={}, bizContent={}", uid, bizContent, e);
        }
        return null;
    }
    
    public String agreementPayNotify(String bizContent, Long uid) {
        try {
            String decrypt = FyAesUtil.decrypt(bizContent, fengYunConfig.getAesKey());
            InstallmentDeductNotifyQuery deductNotifyQuery = JsonUtil.fromJson(decrypt, InstallmentDeductNotifyQuery.class);
            InstallmentDeductionRecord deductionRecord = installmentDeductionRecordService.queryByPayNo(deductNotifyQuery.getPayNo());
            
            // 处理代扣成功的场景
            installmentBizService.handleAgreementPaySuccess(deductionRecord, deductNotifyQuery.getTradeNo());
            
            return "SUCCESS";
        } catch (Exception e) {
            log.error("NOTIFY AGREEMENT PAY ERROR!", e);
        }
        return null;
    }
    
    public void dailyInstallmentDeduct() {
        List<String> externalAgreementNos = installmentDeductionPlanService.listExternalAgreementNoForDeduct(System.currentTimeMillis());
        
        externalAgreementNos.parallelStream().forEach(externalAgreementNo -> {
            InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNoWithoutUnpaid(externalAgreementNo);
            if (Objects.isNull(installmentRecord)) {
                log.warn("DEDUCT TASK WARN! FyConfig is null, externalAgreementNo={}", externalAgreementNo);
                return;
            }
            
            Integer issue = installmentRecord.getPaidInstallment() + 1;
            
            InstallmentDeductionRecordQuery query = new InstallmentDeductionRecordQuery();
            query.setExternalAgreementNo(externalAgreementNo);
            query.setUid(installmentRecord.getUid());
            query.setIssue(issue);
            query.setStatus(DEDUCTION_RECORD_STATUS_INIT);
            List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(query);
            if (!CollectionUtils.isEmpty(installmentDeductionRecords)) {
                log.warn("DEDUCT TASK WARN! Deduction is running, externalAgreementNo={}", externalAgreementNo);
                return;
            }
            
            List<InstallmentDeductionPlan> deductionPlanList = installmentDeductionPlanService.listByExternalAgreementNoAndIssue(installmentRecord.getTenantId(),
                    externalAgreementNo, issue);
            if (CollectionUtils.isEmpty(deductionPlanList)) {
                log.warn("DEDUCT TASK WARN! deductionPlanList is null, externalAgreementNo={}", externalAgreementNo);
                return;
            }
            
            FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(installmentRecord.getTenantId());
            if (Objects.isNull(fyConfig)) {
                log.error("DEDUCT TASK ERROR! FyConfig is null, tenantId={}", installmentRecord.getTenantId());
                return;
            }
            
            Triple<Boolean, String, Object> triple = installmentBizService.initiatingDeduct(deductionPlanList, installmentRecord, fyConfig);
            if (!triple.getLeft()) {
                log.warn("DEDUCT TASK WARN! DeductT fail, uid={}, externalAgreementNo={}", installmentRecord.getUid(), installmentRecord.getExternalAgreementNo());
            }
        });
    }
    
    public void cancelSign() {
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());
        try {
            double now = System.currentTimeMillis();
            double min = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
            
            Map<String, Double> results = getAndDelete(min, now);
            if (CollectionUtils.isEmpty(results)) {
                return;
            }
            
            results.keySet().parallelStream().forEach(externalAgreementNo -> {
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
        String luaScript = "local key = KEYS[1]\n" + "local minScore = ARGV[1]\n" + "local maxScore = ARGV[2]\n"
                + "local results = redis.call('ZRANGEBYSCORE', key, minScore, maxScore, 'WITHSCORES')\n" + "redis.call('ZREMRANGEBYSCORE', key, minScore, maxScore)\n"
                + "return results";
        
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
