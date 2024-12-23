package com.xiliulou.electricity.controller.admin.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.query.installment.InstallmentDeductionRecordQuery;
import com.xiliulou.electricity.service.installment.InstallmentDeductionRecordService;
import com.xiliulou.electricity.vo.installment.InstallmentDeductionRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.PROCESS_PARAMETER_DATA_PERMISSION;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.PROCESS_PARAMETER_LOGIN_AND_DATA_AND_PAGE;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 17:32
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/installment/deductionRecord")
public class JsonAdminInstallmentDeductionRecordController {
    
    private final InstallmentDeductionRecordService installmentDeductionRecordService;
    
    
    @PostMapping("/page")
    @ProcessParameter(type = PROCESS_PARAMETER_LOGIN_AND_DATA_AND_PAGE)
    public R<List<InstallmentDeductionRecordVO>> page(@RequestBody InstallmentDeductionRecordQuery installmentDeductionRecordQuery) {
        return installmentDeductionRecordService.listForPage(installmentDeductionRecordQuery);
    }
    
    
    @PostMapping("/count")
    @ProcessParameter(type = PROCESS_PARAMETER_DATA_PERMISSION)
    public R<Integer> count(@RequestBody InstallmentDeductionRecordQuery installmentDeductionRecordQuery) {
        return installmentDeductionRecordService.count(installmentDeductionRecordQuery);
    }
}
