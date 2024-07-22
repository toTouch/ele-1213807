package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/11 22:28
 * @desc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantPlaceCabinetPageRequest {
    private Long size;
    
    private Long offset;
    
    private Integer status;
    
    private String sn;
    
    private Integer tenantId;
    
    private Long placeId;
    
    private List<Long> bindFranchiseeIdList;
}
