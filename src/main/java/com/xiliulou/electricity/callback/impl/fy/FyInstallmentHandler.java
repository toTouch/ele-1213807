package com.xiliulou.electricity.callback.impl.fy;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
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
import com.xiliulou.pay.deposit.fengyun.config.FengYunConfig;
import com.xiliulou.pay.deposit.fengyun.utils.FyAesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
    

    public String signNotify(String bizContent, Long uid) {
        try {
            String decrypt = FyAesUtil.decrypt(bizContent, fengYunConfig.getAesKey());
            InstallmentSignNotifyQuery signNotifyQuery = JsonUtil.fromJson(decrypt, InstallmentSignNotifyQuery.class);
            
            log.info("回调调试，signNotifyQuery={}", JsonUtil.toJson(signNotifyQuery));
            InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNoWithoutUnpaid(signNotifyQuery.getExternalAgreementNo());
            
            R<String> stringR;
            if (NOTIFY_STATUS_SIGN.equals(Integer.valueOf(signNotifyQuery.getStatus()))) {
                log.info("回调调试，签约回调");
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
            InstallmentDeductionPlan deductionPlan = installmentDeductionPlanService.queryPlanForDeductByAgreementNo(externalAgreementNo);
            
            InstallmentDeductionRecordQuery query = new InstallmentDeductionRecordQuery();
            query.setExternalAgreementNo(deductionPlan.getExternalAgreementNo());
            query.setStatus(DEDUCTION_RECORD_STATUS_INIT);
            List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(query);
            if (!CollectionUtils.isEmpty(installmentDeductionRecords)) {
                return;
            }
            
            InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNoWithoutUnpaid(externalAgreementNo);
            
            FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(deductionPlan.getTenantId());
            if (Objects.isNull(fyConfig)) {
                log.error("DEDUCT TASK ERROR! FyConfig is null, tenantId={}", deductionPlan.getTenantId());
            }
            
            installmentBizService.initiatingDeduct(deductionPlan, installmentRecord, fyConfig);
        });
    }
}
