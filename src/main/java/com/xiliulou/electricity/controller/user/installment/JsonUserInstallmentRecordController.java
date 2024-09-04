package com.xiliulou.electricity.controller.user.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.query.installment.InstallmentPayQuery;
import com.xiliulou.electricity.query.installment.InstallmentSignQuery;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FySignAgreementRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FySignAgreementRsp;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:35
 */
@RestController
@RequestMapping("/user/Installment/record")
@AllArgsConstructor
public class JsonUserInstallmentRecordController {
    
    private InstallmentRecordService installmentRecordService;
    
    
    /**
     * 签约接口
     */
    @ProcessParameter
    @PostMapping("/sign")
    public R<Object> sign(@Validated @RequestBody InstallmentSignQuery query, HttpServletRequest request) {
        return installmentRecordService.sign(query, request);
    }
}
