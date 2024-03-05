package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : MerchantPromotionRenewalQueryModel
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-22
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPromotionRenewalQueryModel {
    private Integer status;
    
    /**
     * 用户类型
     */
    private Integer userType;
    
    /**
     * 返利类型
     */
    private Integer rebateType;
    
    private Integer tenantId;
    
    private Long uid;
    
    private Long startTime;
    
    private Long endTime;
    
}
