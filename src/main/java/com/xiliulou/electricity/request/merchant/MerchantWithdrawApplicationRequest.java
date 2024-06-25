package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/20 14:26
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantWithdrawApplicationRequest {

    private Long id;
    
    private Long uid;
    
    private Integer status;
    
    private String orderNo;
    
    private String remark;
    
    private Long beginTime;
    
    private Long endTime;
    
    /**
     * 提现金额， 单位元
     */
    @NotNull(message = "提现金额不能为空")
    private BigDecimal amount;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    private List<Long> franchiseeIds;
    
    private Long size;
    
    private Long offset;
    
    /**
     * 支付机构返回的批次单号
     */
    private String transactionBatchId;
    
    /**
     * 支付机构返回的批次明细单号
     */
    private String transactionDetailId;
    
    /**
     * 审核时间开始
     */
    private Long checkTimeStart;
    
    /**
     * 审核时间结束
     */
    private Long checkTimeEnd;
    
}
