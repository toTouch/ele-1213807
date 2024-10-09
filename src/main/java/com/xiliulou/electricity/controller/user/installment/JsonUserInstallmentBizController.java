package com.xiliulou.electricity.controller.user.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.query.installment.CreateTerminatingRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentSignQuery;
import com.xiliulou.electricity.service.installment.InstallmentBizService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.CHANNEL_FROM_H5;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.CHANNEL_FROM_MINIAPP;

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
        return installmentBizService.sign(query, request, CHANNEL_FROM_H5);
    }
    
    /**
     * 签约接口
     */
    @PostMapping("/user/Installment/record/alipaySign")
    public R<String> alipaySign(@Validated @RequestBody InstallmentSignQuery query, HttpServletRequest request) {
        R<String> sign = installmentBizService.sign(query, request, CHANNEL_FROM_MINIAPP);
        if (!sign.isSuccess() || StringUtils.isBlank(sign.getData())) {
            return sign;
        } else {
            String data = sign.getData();
            // 去除多余的引号
            if (data.startsWith("\"") && data.endsWith("\"")) {
                // 使用 substring 方法去掉开头和结尾的双引号
                String modifiedData = data.substring(1, data.length() - 1);
                return R.ok(modifiedData);
            } else {
                return sign;
            }
        }
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
