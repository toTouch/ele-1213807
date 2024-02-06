package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 区域查询
 * @date 2024/2/6 15:34:16
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantAreaQuery {
    
    private Long size;
    
    private Long offset;
    
    private Long id;
    
    private String name;
    
    private Integer tenantId;
    
}
