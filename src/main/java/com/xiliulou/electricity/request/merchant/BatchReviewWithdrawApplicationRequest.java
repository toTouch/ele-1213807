package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/24 15:00
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BatchReviewWithdrawApplicationRequest {
    
    private List<Long> ids;
    
    private Integer status;
    
    private String remark;
    
    private String batchNo;
    
    private Integer tenantId;
    
}
