package com.xiliulou.electricity.controller.user.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.query.installment.InstallmentDeductionPlanQuery;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.vo.installment.InstallmentDeductionPlanAssemblyVO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/6 19:08
 */
@RestController
@RequestMapping("/user/installment/deductionPlan")
@AllArgsConstructor
public class JsonUserInstallmentDeductionPlanController {
    
    private InstallmentDeductionPlanService installmentDeductionPlanService;
    
    @GetMapping("/listDeductionPlanForRecord")
    public R<List<InstallmentDeductionPlan>> listDeductionPlanForRecordUser(@RequestParam(value = "externalAgreementNo") String externalAgreementNo) {
        InstallmentDeductionPlanQuery query = new InstallmentDeductionPlanQuery();
        query.setExternalAgreementNo(externalAgreementNo);
        
        return installmentDeductionPlanService.listDeductionPlanByAgreementNo(query);
    }
    

}
