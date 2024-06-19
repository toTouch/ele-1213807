package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName : MerchantPlaceFeeMonthSummaryRecordQuery
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-20
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPlaceFeeMonthSummaryRecordQueryModel
{
    private String monthDate;
    
    private Long size;
    
    private Long offset;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    /**
     * 类型：0-租户，1-加盟商
     */
    private Integer type;
}
