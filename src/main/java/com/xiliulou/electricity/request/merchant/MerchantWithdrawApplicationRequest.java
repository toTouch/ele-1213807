package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    
    private Long merchantUid;
    
    private Integer status;
    
    private Long beginTime;
    
    private Long endTime;
    
    /**
     * 提现金额， 单位元
     */
    private BigDecimal amount;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    private Long size;
    
    private Long offset;
    
}
