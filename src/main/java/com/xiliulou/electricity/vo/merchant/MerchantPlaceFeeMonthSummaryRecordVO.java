package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName : MerchantPlaceFeeMonthRecord
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-19
 */

@Data
public class MerchantPlaceFeeMonthSummaryRecordVO {
    private String monthDate;
    
    private Integer placeNum;
    
    private BigDecimal monthPlaceFee;
}
