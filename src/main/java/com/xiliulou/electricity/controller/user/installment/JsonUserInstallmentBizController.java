package com.xiliulou.electricity.controller.user.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.query.installment.CreateTerminatingRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentSignQuery;
import com.xiliulou.electricity.service.installment.InstallmentBizService;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/7 22:33
 */
@RestController
@AllArgsConstructor
public class JsonUserInstallmentBizController {
    
    private InstallmentBizService installmentBizService;
    
    /**
     * 签约接口
     */
    @PostMapping("/user/Installment/record/sign")
    public R<String> sign(@Validated @RequestBody InstallmentSignQuery query, HttpServletRequest request) {
        return installmentBizService.sign(query, request);
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
    @GetMapping("/user/installment/deductionPlan/deduct")
    public R deduct(@RequestParam Long id) {
        return installmentBizService.deduct(id);
    }
    
    /**
     * 用户端申请解约
     */
    @PostMapping("/user/Installment/Terminating/create")
    public R<String> terminatingRecord(@RequestBody @Validated CreateTerminatingRecordQuery query) {
        return installmentBizService.createTerminatingRecord(query.getExternalAgreementNo(), query.getReason());
    }
}
