package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/2/21 20:45
 * @desc
 */
@Data
public class ChannelEmployeePromotionVO {
    /**
     * 出账日期
     */
    private Long feeDate;
    
    /**
     * 渠道员数量
     */
    private Integer channelEmployeeCount;
    
    /**
     * 月拉新返现汇总(元)
     */
    private BigDecimal monthFirstMoney;
    
    /**
     * 月续费返现汇总(元)
     */
    private BigDecimal monthRenewMoney;
    /**
     * 月费用总额(元)
     */
    private BigDecimal monthTotalMoney;
    
    /**
     * 出账日期：yyyy-MM
     */
    private String billingDate;
}
