package com.xiliulou.electricity.controller.admin.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.query.installment.InstallmentTerminatingRecordQuery;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import com.xiliulou.electricity.vo.installment.InstallmentTerminatingRecordVO;
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
 * @Date: 2024/8/28 10:50
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/installment/terminating")
public class JsonAdminInstallmentTerminatingRecordController {
    
    private final InstallmentTerminatingRecordService installmentTerminatingRecordService;
    
    
    @PostMapping("/page")
    @ProcessParameter(type = PROCESS_PARAMETER_LOGIN_AND_DATA_AND_PAGE)
    public R<List<InstallmentTerminatingRecordVO>> page(@RequestBody InstallmentTerminatingRecordQuery query) {
        return installmentTerminatingRecordService.listForPage(query);
    }
    
    
    @PostMapping("/count")
    @ProcessParameter(type = PROCESS_PARAMETER_DATA_PERMISSION)
    public R<Integer> count(@RequestBody InstallmentTerminatingRecordQuery query) {
        return installmentTerminatingRecordService.count(query);
    }
    
    
}
