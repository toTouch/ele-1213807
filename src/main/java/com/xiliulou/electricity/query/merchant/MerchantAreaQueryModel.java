package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 区域查询
 * @date 2024/2/6 15:34:16
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantAreaQueryModel {
    
    private Long size;
    
    private Long offset;
    
    private Long id;
    
    private String name;
    
    private Integer tenantId;
    
    private List<Long> franchiseeIdList;
    
    private List<Long> idList;
    
}
