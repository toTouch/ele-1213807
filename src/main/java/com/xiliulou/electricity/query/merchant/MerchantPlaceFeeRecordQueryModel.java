package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/2/17 22:57
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPlaceFeeRecordQueryModel {
    
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
