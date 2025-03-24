package com.xiliulou.electricity.controller.admin.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.vo.installment.InstallmentRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.PROCESS_PARAMETER_DATA_PERMISSION;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.PROCESS_PARAMETER_LOGIN_AND_DATA_AND_PAGE;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 9:38
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/installment/record")
public class JsonAdminInstallmentRecordController {

    private final InstallmentRecordService installmentRecordService;


    @PostMapping("/page")
    @ProcessParameter(type = PROCESS_PARAMETER_LOGIN_AND_DATA_AND_PAGE)
    public R<List<InstallmentRecordVO>> page(@RequestBody InstallmentRecordQuery installmentRecordQuery) {
        return installmentRecordService.listForPage(installmentRecordQuery);
    }


    @PostMapping("/count")
    @ProcessParameter(type = PROCESS_PARAMETER_DATA_PERMISSION)
    public R<Integer> count(@RequestBody InstallmentRecordQuery installmentRecordQuery) {
        return installmentRecordService.count(installmentRecordQuery);
    }

}
