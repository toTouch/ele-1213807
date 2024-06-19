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
    
    private String monthRentDays;
    
    private BigDecimal monthTotalPlaceFee;
    
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
    
    /**
     * 柜机与场地是否绑定：0:绑定，1：解绑
     */
    private Integer cabinetPlaceBindStatus;
    
    /**
     * 场地柜机在最后一天是否绑定：0：否，1：是
     */
    private Integer cabinetEndBind;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
}

