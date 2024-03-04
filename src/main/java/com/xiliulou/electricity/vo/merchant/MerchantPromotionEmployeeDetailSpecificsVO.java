package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName : MerchantPromotionEmployeeDetailSpecificsVO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-24
 */
@Data
public class MerchantPromotionEmployeeDetailSpecificsVO {
    private Long uid;
    
    private String name;
    
    private String phone;
    
    /**
     * 套餐id
     */
    private Long memberCardId;
    private String batteryMemberCardName;
    
    private Long rebateTime;
    
    private BigDecimal merchantRebate;
    
    private Integer status;
}
