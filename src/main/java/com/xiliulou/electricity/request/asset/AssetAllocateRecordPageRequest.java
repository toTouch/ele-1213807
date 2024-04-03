package com.xiliulou.electricity.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author HeYafeng
 * @description 资产调拨分页request
 * @date 2023/11/29 11:34:19
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetAllocateRecordPageRequest {
    
    private String orderNo;
    
    private Integer type;
    
    private Long sourceFranchiseeId;
    
    private Long targetFranchiseeId;
    
    private Long size;
    
    private Long offset;
    
}
