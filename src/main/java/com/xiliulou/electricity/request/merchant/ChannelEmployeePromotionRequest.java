package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/2/21 20:34
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChannelEmployeePromotionRequest {
    /**
     * 偏移量
     */
    private Integer offset;
    
    /**
     * 取值数量
     */
    private Integer size;
    
    /**
     * 出账年月日
     */
    private String monthDate;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
}
