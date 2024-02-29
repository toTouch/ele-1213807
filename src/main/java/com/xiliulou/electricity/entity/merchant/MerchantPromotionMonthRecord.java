package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description saas端商户推广费月度统计
 * @date 2024/2/23 20:47:44
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@TableName("t_merchant_promotion_month_record")
public class MerchantPromotionMonthRecord {
    
    private Long id;
    
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
    
    private Integer delFlag;
    
    private Long createTime;
    
    private Long updateTime;
    
    private String remark;
}
