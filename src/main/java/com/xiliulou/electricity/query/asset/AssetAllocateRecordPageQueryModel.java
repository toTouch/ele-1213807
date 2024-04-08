package com.xiliulou.electricity.query.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产调拨分页model
 * @date 2023/11/29 11:34:19
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetAllocateRecordPageQueryModel {
    
    private Integer tenantId;
    
    private String orderNo;
    
    private Integer type;
    
    private Long sourceFranchiseeId;
    
    private Long targetFranchiseeId;
    
    private Long size;
    
    private Long offset;
    
    private List<Long> franchiseeIds;
}
