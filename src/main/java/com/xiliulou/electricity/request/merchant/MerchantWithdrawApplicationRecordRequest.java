package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/24 14:24
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantWithdrawApplicationRecordRequest {
    
    private Long merchantUid;
    
    private String batchNo;
    
    private String orderNo;
    
    private String transactionBatchId;
    
    private Integer status;
    
    private String remark;
    
    private Long beginTime;
    
    private Long endTime;
    
    
}
