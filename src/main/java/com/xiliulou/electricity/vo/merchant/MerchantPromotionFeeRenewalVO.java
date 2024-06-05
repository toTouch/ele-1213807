package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @ClassName : MerchantPromotionFeeRenewalVO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
@Data
public class MerchantPromotionFeeRenewalVO {
    
    /**
     * 今日续费次数
     */
    private Integer todayRenewalCount;
    
    /**
     * 昨日续费次数
     */
    private Integer yesterdayRenewalCount;
    
    /**
     * 本月续费次数
     */
    private Integer currentMonthRenewalCount;
    
    /**
     * 上月续费次数
     */
    private Integer lastMonthRenewalCount;
    
    /**
     * 累计续费次数
     */
    private Integer totalRenewalCount;
}
