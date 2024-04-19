package com.xiliulou.electricity.dto.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName : MerchantPlaceFeeMonthRecordDTO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-03-02
 */

@Data
public class MerchantPlaceFeeMonthRecordDTO {
    
    private Long placeId;
    
    
    private String monthDate;
    
    private Integer monthRentDays;
    
    private BigDecimal monthPlaceFee;
    
    private Integer tenantId;
}
