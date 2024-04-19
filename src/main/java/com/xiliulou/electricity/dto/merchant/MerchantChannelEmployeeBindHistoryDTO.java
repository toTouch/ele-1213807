package com.xiliulou.electricity.dto.merchant;

import lombok.Data;

/**
 * @ClassName : MerchantChannelEmployeeBindHistoryDTO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-03-11
 */
@Data
public class MerchantChannelEmployeeBindHistoryDTO {
    private Long merchantUid;
    
    private Long channelEmployeeUid;
    
    private Integer bindStatus;
    
    private Long queryStartTime;
    
    private Long queryEndTime;
    
    private Integer tenantId;
}
