package com.xiliulou.electricity.bo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 *  商户提现发送BO
 */
@Data
public class MerchantWithdrawSendBO {
    /**
     * 提现申请id
     */
    private Long applicationId;

    /**
     * 提现记录id
     */
    private Long recordId;

    /**
     * 提现金额
     */
    private BigDecimal amount;

    /**
     * 提现批次号详情
     */
    private String batchDetailNo;

    /**
     * 提现批次号
     */
    private String batchNo;

    /**
     * 商户uid
     */
    private Long uid;

    private Long franchiseeId;

    private Long tenantId;
}
