package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 场地电费统计
 * @date 2024/2/24 18:24:49
 */
@Data
public class MerchantCabinetPowerMonthRecordVO {
    
    private Long id;
    
    /**
     * 出账年月 yyyy-MM
     */
    private String date;
    
    /**
     * 场地数
     */
    private Integer placeCount;
    
    /**
     * 当月耗电量
     */
    private Double monthSumPower;
    
    /**
     * 当月电费
     */
    private Double monthSumCharge;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    
    private String franchiseeName;
    
}
