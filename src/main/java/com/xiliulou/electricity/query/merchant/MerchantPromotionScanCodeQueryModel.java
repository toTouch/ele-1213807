package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : MerchantPromotionScanCodeQueryModel
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-22
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPromotionScanCodeQueryModel {
    private Integer type;
    
    private Long uid;
    
    private Integer status;
    
    private Long channelEmployeeUid;
    
    private Integer tenantId;
    
    private Long startTime;
    
    private Long endTime;
    
    
}
