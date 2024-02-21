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
@TableName("t_merchant_place_fee_month_record")
public class MerchantPlaceFeeMonthRecord {
    private Long id;
    
    private String monthDate;
    
    private Long placeId;
    
    private Long eid;
    
    private String sn;
    
    private Long rentStartTime;
    
    private Long rentEndTime;
    
    private Integer rentDays;
    
    private BigDecimal placeFee;
    
    private BigDecimal monthPlaceFee;
    
    private Integer tenantId;
    
    private Integer delFlag;
    
    private Long createTime;
    
    private Long updateTime;
}

