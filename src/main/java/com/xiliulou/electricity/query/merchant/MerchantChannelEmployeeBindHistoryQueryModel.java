package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : MerchantChannelEmployeeBindHistoryQueryModel
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-03-11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantChannelEmployeeBindHistoryQueryModel {
    private Long channelEmployeeUid;
    
    private Long merchantUid;
    
    private Integer bindStatus;
    
    private Long bindTime;
    
    private Long unBindTime;
    
    private Integer tenantId;
}
