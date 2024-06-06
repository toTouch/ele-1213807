package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/21 13:37
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantWithdrawApplicationVO {
    
    /**
     * 提现申请ID
     */
    private Long id;
    
    /**
     * 提现订单编号
     */
    private String orderNo;
    
    /**
     * 提现商户UID
     */
    private String uid;
    
    /**
     * 商户名称
     */
    private String name;
    
    /**
     * 商户电话
     */
    private String phone;
    
    /**
     * 申请提现金额 单位：元
     */
    private BigDecimal amount;
    
    /**
     * 审批原因
     */
    private String remark;
    
    /**
     * 状态 审核状态 1--审核中 2--审核拒绝  3--审核通过  4--提现中  5--提现成功  6--提现失败
     */
    private Integer status;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 提交时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 审核时间
     */
    private Long checkTime;
    
    /**
     * 到账时间
     */
    private Long receiptTime;
    
    /**
     * 失败原因，用于页面友好提示
     */
    private String failReason;
    
    /**
     * 真实原因，微信侧返回的错误原因
     */
    private String realReason;
    
    /**
     * 支付机构返回的批次单号
     */
    private String transactionBatchId;
    
    /**
     * 支付机构返回的批次明细单号
     * transactionDetailId
     */
    private String transactionDetailId;
    
}
