package com.xiliulou.electricity.service.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.query.installment.HandleTerminatingRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentSignQuery;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/7 22:20
 */
public interface InstallmentBizService {
    
    /**
     * 同步签约状态
     * @param externalAgreementNo 请求签约号
     * @return 同步结果
     */
    R querySignStatus(String externalAgreementNo);
    
    /**
     * 生成解约记录并解约
     * @param externalAgreementNo 请求签约号
     * @return 解约结果
     */
    R terminateRecord(String externalAgreementNo);
    
    /**
     * 审核解约申请
     * @param query 参数
     * @return 审核是否成功
     */
    R<String> handleTerminatingRecord(HandleTerminatingRecordQuery query);
    
    /**
     * 同步代扣状态
     * @param payNo 扣款订单号
     * @return 同步结果
     */
    R queryDeductStatus(String payNo);
    
    /**
     * 用户端分期代扣
     *
     * @param id 请求签约号
     * @return 代扣调用结果
     */
    R<String> deduct(Long id);
    
    /**
     * 请求峰云签约接口返回二维码链接
     */
    R<String> sign(InstallmentSignQuery query, HttpServletRequest request);
    
    /**
     * 签约成功的处理
     * @param installmentRecord 签约记录
     * @param agreementNo 用户签约成功记录编号，代扣使用
     * @return 处理结果
     */
    R<String> handleSign(InstallmentRecord installmentRecord, String agreementNo);
    
    /**
     * 解约成功的处理
     * @param installmentRecord 签约记录
     * @return 处理结果
     */
    R<String> handleTerminating(InstallmentRecord installmentRecord);
    
    /**
     * 代扣成功的处理
     * @param deductionRecord 代扣记录
     * @param tradeNo 支付宝交易号
     * @return 处理结果
     */
    R handleAgreementPaySuccess(InstallmentDeductionRecord deductionRecord, String tradeNo);
    
    /**
     * 调起代扣
     *
     * @param deductionPlan     代扣计划
     * @param installmentRecord 签约记录
     * @param fyConfig          蜂云配置
     * @return 调起结果
     */
    Triple<Boolean, String, Object> initiatingDeduct(InstallmentDeductionPlan deductionPlan, InstallmentRecord installmentRecord, FyConfig fyConfig);
    
    /**
     * 用户端申请解约
     * @param externalAgreementNo 请求签约号
     * @param reason 原因
     * @return 申请结果
     */
    R<String> createTerminatingRecord(String externalAgreementNo, String reason);
}
