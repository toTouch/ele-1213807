package com.xiliulou.electricity.callback.impl.fy;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.query.installment.InstallmentDeductNotifyQuery;
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

import java.util.List;

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
            InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(signNotifyQuery.getExternalAgreementNo());
            
            R<String> stringR;
            if (NOTIFY_STATUS_SIGN.equals(Integer.valueOf(signNotifyQuery.getStatus()))) {
                log.info("回调调试，签约回调");
                stringR = installmentBizService.handleSign(installmentRecord);
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
            installmentBizService.handleAgreementPaySuccess(deductionRecord);
            
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
            
            FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(deductionPlan.getTenantId());
            
            InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(externalAgreementNo);
            
            installmentBizService.initiatingDeduct(deductionPlan, installmentRecord, fyConfig);
        });
    }
}
