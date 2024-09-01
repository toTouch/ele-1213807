package com.xiliulou.electricity.controller.user.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.query.installment.InstallmentPayQuery;
import com.xiliulou.electricity.query.installment.InstallmentSignQuery;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FySignAgreementRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FySignAgreementRsp;
import org.springframework.beans.factory.annotation.Autowired;
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
public class JsonUserInstallmentRecordController {
    
    @Autowired
    private InstallmentRecordService installmentRecordService;

    /**
     * 购买分期套餐
     */
    @ProcessParameter
    @PostMapping("/pay")
    public R<Object> pay(@RequestBody InstallmentPayQuery query, HttpServletRequest request) {
        return installmentRecordService.pay(query, request);
    }

    @ProcessParameter
    @PostMapping("/sign")
    public R<Object> sign(@RequestBody InstallmentSignQuery query, HttpServletRequest request) {
        return installmentRecordService.sign(query, request);
    }
}
