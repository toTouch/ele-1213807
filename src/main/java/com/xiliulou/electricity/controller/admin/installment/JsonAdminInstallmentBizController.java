package com.xiliulou.electricity.controller.admin.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.installment.HandleTerminatingRecordQuery;
import com.xiliulou.electricity.service.installment.InstallmentBizService;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/7 22:16
 */
@RestController
@AllArgsConstructor
public class JsonAdminInstallmentBizController {
    
    private InstallmentBizService installmentBizService;
    
    /**
     * 后台同步签约结果
     */
    @GetMapping("/admin/installment/record/queryStatus")
    public R<String> querySignStatus(@RequestParam String externalAgreementNo) {
        return installmentBizService.querySignStatus(externalAgreementNo);
    }
    
    /**
     * 后台解约
     */
    @GetMapping("/admin/installment/record/terminate")
    public R<String> terminateRecord(@RequestParam String externalAgreementNo) {
        return installmentBizService.terminateRecord(externalAgreementNo);
    }
    
    /**
     * 后台审核解约申请
     */
    @PostMapping("/admin/installment/terminating/handle")
    public R<String> handleTerminatingRecord(@RequestBody @Validated HandleTerminatingRecordQuery query) {
        return installmentBizService.handleTerminatingRecord(query);
    }
    
    /**
     * 后台同步代扣结果
     */
    @GetMapping("/admin/installment/deductionRecord/queryStatus")
    public R queryDeductStatus(@RequestParam String payNo) {
        return installmentBizService.queryDeductStatus(payNo);
    }
}
