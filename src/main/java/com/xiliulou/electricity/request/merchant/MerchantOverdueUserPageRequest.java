package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/19 21:15
 * @desc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantOverdueUserPageRequest {
    private Long size;
    
    private Long offset;
    
    private Long merchantId;
    
    private Integer tenantId;
}
