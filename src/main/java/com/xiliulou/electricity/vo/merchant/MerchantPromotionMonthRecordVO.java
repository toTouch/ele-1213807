package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 商户推广费月结统计
 * @date 2024/2/24 11:12:09
 */
@Data
public class MerchantPromotionMonthRecordVO {
    
    private Long id;
    
    /**
     * 商户数量
     */
    private Integer merchantCount;
    
    /**
     * 月拉新返现汇总(元)
     */
    private BigDecimal monthFirstTotalMoney;
    
    /**
     * 月续费返现汇总(元)
     */
    private BigDecimal monthRenewTotalMoney;
    
    /**
     * 月费用总额(元)
     */
    private BigDecimal monthTotalMoney;
    
    private String date;
    
    private Integer tenantId;
    
    private Long franchiseeId;
}
