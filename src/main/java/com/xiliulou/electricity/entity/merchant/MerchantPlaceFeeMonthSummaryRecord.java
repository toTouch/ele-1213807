package com.xiliulou.electricity.entity.merchant;

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
@TableName("t_merchant_place_fee_month_summary_record")
public class MerchantPlaceFeeMonthSummaryRecord {
    private Long id;
    
    private String monthDate;
    
    private Integer placeNum;
    
    private BigDecimal monthPlaceFee;
    
    private Integer tenantId;
    
    private Integer delFlag;
    
    private Long createTime;
    
    private Long updateTime;
}
