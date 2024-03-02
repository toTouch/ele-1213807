package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName : MerchantPlaceFeeMonthRecordExportVO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-26
 */
@Data
public class MerchantPlaceFeeMonthRecordExportVO {
    
    private String monthDate;
    
    private String placeName;
    
    private String monthRentDays;
    
    private BigDecimal monthTotalPlaceFee;
    
    private String sn;
    
    private Integer rentDays;
    
    private String rentStartTime;
    
    private String rentEndTime;
    
    private BigDecimal placeFee;
    
    private BigDecimal monthPlaceFee;
}
