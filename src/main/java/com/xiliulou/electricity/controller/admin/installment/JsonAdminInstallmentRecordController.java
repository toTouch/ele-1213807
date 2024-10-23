package com.xiliulou.electricity.controller.admin.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.PROCESS_PARAMETER_DATA_PERMISSION;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.PROCESS_PARAMETER_LOGIN_AND_DATA_AND_PAGE;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 9:38
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/installment/record")
public class JsonAdminInstallmentRecordController {
    
    private final InstallmentRecordService installmentRecordService;
    
    
    @PostMapping("/page")
    @ProcessParameter(type = PROCESS_PARAMETER_LOGIN_AND_DATA_AND_PAGE)
    public R page(@RequestBody InstallmentRecordQuery installmentRecordQuery) {
        return installmentRecordService.listForPage(installmentRecordQuery);
    }
    
    
    @PostMapping("/count")
    @ProcessParameter(type = PROCESS_PARAMETER_DATA_PERMISSION)
    public R count(@RequestBody InstallmentRecordQuery installmentRecordQuery) {
        return installmentRecordService.count(installmentRecordQuery);
    }
    
    
}
