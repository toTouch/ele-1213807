package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/24 14:13
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantWithdrawApplicationRecordVO {
    
    private Long id;
    
    private Long uid;
    
    private BigDecimal amount;
    
    private Integer status;
    
    private String transaction_no;
    
    private String orderNo;
    
    private String batchNo;
    
    private String response;
    
    private Integer tenantId;
    
    private String remark;
    
    private Long createTime;
    
    private Long updateTime;
    
}
