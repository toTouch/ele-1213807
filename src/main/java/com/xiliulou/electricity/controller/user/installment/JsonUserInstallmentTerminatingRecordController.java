package com.xiliulou.electricity.controller.user.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.query.installment.CreateTerminatingRecordQuery;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/5 15:48
 */
@RestController
@RequestMapping("/user/Installment/Terminating")
@AllArgsConstructor
public class JsonUserInstallmentTerminatingRecordController {
    
    private InstallmentTerminatingRecordService installmentTerminatingRecordService;
    
    @ProcessParameter
    @PostMapping("/create")
    public R<String> terminatingRecord(@RequestBody @Validated CreateTerminatingRecordQuery query) {
        return installmentTerminatingRecordService.createTerminatingRecord(query.getExternalAgreementNo(), query.getReason());
    }
}
