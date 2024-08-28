package com.xiliulou.electricity.controller.admin.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.query.installment.InstallmentTerminatingRecordQuery;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import com.xiliulou.electricity.vo.installment.InstallmentTerminatingRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/28 10:50
 */
@RestController
@Slf4j
@RequestMapping("/admin/installment/terminatingRecord")
public class JsonAdminInstallmentTerminatingRecordController {
    
    @Autowired
    private InstallmentTerminatingRecordService installmentTerminatingRecordService;
    
    @ProcessParameter(type = InstallmentConstants.PROCESS_PARAMETER_TYPE_PAGE)
    @PostMapping("/page")
    public R<List<InstallmentTerminatingRecordVO>> page(InstallmentTerminatingRecordQuery query) {
        return installmentTerminatingRecordService.listForPage(query);
    }
    
    @ProcessParameter()
    @PostMapping("/count")
    public R<Integer> count(InstallmentTerminatingRecordQuery query) {
        return installmentTerminatingRecordService.count(query);
    }
}
