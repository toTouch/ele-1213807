package com.xiliulou.electricity.vo.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName : MerchantPlaceFeeMonthRecord
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-19
 */

@Data
public class MerchantPlaceFeeMonthRecordVO {
    
    private String monthDate;
    
    private String placeName;
    
    private String monthRentDays;
    
    private BigDecimal monthTotalPlaceFee;
    
    private String sn;
    
    private String rentStartTime;
    
    private String rentEndTime;
    
    private String rentDays;
    
    private BigDecimal placeFee;
    
    private BigDecimal monthPlaceFee;
}

