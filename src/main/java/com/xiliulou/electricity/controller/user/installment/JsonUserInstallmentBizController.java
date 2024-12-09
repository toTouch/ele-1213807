package com.xiliulou.electricity.controller.user.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.query.installment.CreateTerminatingRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentSignQuery;
import com.xiliulou.electricity.service.installment.InstallmentBizService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.CHANNEL_FROM_H5;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.CHANNEL_FROM_MINIAPP;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/7 22:33
 */
@RestController
@RequiredArgsConstructor
public class JsonUserInstallmentBizController {
    
    private final InstallmentBizService installmentBizService;
    
    private final InstallmentDeductionPlanService installmentDeductionPlanService;
    
    
    /**
     * 签约接口
     */
    @PostMapping("/user/Installment/record/sign")
    public R<String> sign(@Validated @RequestBody InstallmentSignQuery query, HttpServletRequest request) {
        return installmentBizService.sign(query, request, CHANNEL_FROM_H5);
    }
    
    /**
     * 签约接口
     */
    @PostMapping("/user/Installment/record/alipaySign")
    public R<String> alipaySign(@Validated @RequestBody InstallmentSignQuery query, HttpServletRequest request) {
        R<String> sign = installmentBizService.sign(query, request, CHANNEL_FROM_MINIAPP);
        if (!sign.isSuccess() || StringUtils.isBlank(sign.getData())) {
            return sign;
        } else {
            String data = sign.getData();
            // 去除多余的引号
            if (data.startsWith("\"") && data.endsWith("\"")) {
                // 使用 substring 方法去掉开头和结尾的双引号
                String modifiedData = data.substring(1, data.length() - 1);
                return R.ok(modifiedData);
            } else {
                return sign;
            }
        }
    }
    
    /**
     * 用户端同步签约状态
     */
    @GetMapping("/user/Installment/record/queryStatus")
    public R<String> queryStatus(@RequestParam String externalAgreementNo) {
        return installmentBizService.querySignStatus(externalAgreementNo);
    }
    
    /**
     * 用户端代扣
     */
    @Deprecated
    @GetMapping("/user/installment/deductionPlan/deduct")
    public R<String> deduct(@RequestParam Long id) {
        // TODO 兼容旧小程序
        InstallmentDeductionPlan deductionPlan = installmentDeductionPlanService.queryById(id);
        List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listByExternalAgreementNoAndIssue(TenantContextHolder.getTenantId(),
                deductionPlan.getExternalAgreementNo(), deductionPlan.getIssue());
        
        return installmentBizService.deduct(deductionPlans);
    }
    
    /**
     * 用户端代扣
     */
    @GetMapping("/user/installment/deductionPlan/userDeduct")
    public R<String> userDeduct(@RequestParam String externalAgreementNo, @RequestParam Integer issue) {
        // TODO 兼容旧小程序，暂时在controller层查询数据，待旧接口不再使用，删除旧接口后调整结构
        List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listByExternalAgreementNoAndIssue(TenantContextHolder.getTenantId(), externalAgreementNo,
                issue);
        
        return installmentBizService.deduct(deductionPlans);
    }
    
    /**
     * 用户端申请解约
     */
    @PostMapping("/user/Installment/Terminating/create")
    public R<String> terminatingRecord(@RequestBody @Validated CreateTerminatingRecordQuery query) {
        return installmentBizService.createTerminatingRecord(query.getExternalAgreementNo(), query.getReason());
    }
}
