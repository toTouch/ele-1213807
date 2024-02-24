package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/2/17 22:44
 * @desc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantPlaceFeeRecordPageRequest {
    private Long size;
    
    private Long offset;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 柜机id
     */
    private Integer cabinetId;
}
